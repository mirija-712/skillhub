<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Illuminate\Http\Client\ConnectionException;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;
use Symfony\Component\HttpFoundation\Response;

/**
 * Authentifie les requêtes API Laravel en déléguant la validation du jeton au service Spring {@code authentification_back}.
 *
 * <p><b>Pourquoi</b> : les comptes et le jeton de session sont gérés par Spring (table {@code utilisateurs}).
 * Laravel ne délivre plus de JWT pour le flux SkillHub ; il vérifie chaque requête protégée en appelant
 * {@code GET {AUTHENTIFICATION_API_URL}/auth/me} avec le même {@code Authorization: Bearer} que le front.
 *
 * <p><b>Effet</b> : si la réponse Spring est OK, un objet utilisateur minimal ({@code id}, {@code email}, {@code nom},
 * {@code prenom}, {@code role}) est injecté via {@code $request->setUserResolver} pour que les middlewares
 * {@code formateur} / {@code apprenant} et les contrôleurs continuent d'utiliser {@code $request->user()}.
 *
 * <p><b>Configuration</b> : {@code config('services.authentification.base_url')} (env {@code AUTHENTIFICATION_API_URL}).
 */
class AuthenticateWithAuthService
{
    public function handle(Request $request, Closure $next): Response
    {
        $token = $this->extractBearerToken($request);
        if (! $token) {
            return response()->json(['message' => 'Token manquant ou invalide. Veuillez vous reconnecter.'], 401);
        }

        $configuredBaseUrl = rtrim((string) config('services.authentification.base_url', ''), '/');
        $authBaseUrls = $this->buildAuthBaseUrls($configuredBaseUrl);
        $lastConnectionError = null;
        $response = null;
        $resolvedBaseUrl = null;

        foreach ($authBaseUrls as $authBaseUrl) {
            try {
                $candidateResponse = Http::timeout((int) config('services.authentification.timeout', 8))
                    ->retry(2, 200, throw: false)
                    ->acceptJson()
                    ->withToken($token)
                    ->get($authBaseUrl.'/auth/me');

                $response = $candidateResponse;
                $resolvedBaseUrl = $authBaseUrl;
                break;
            } catch (ConnectionException $e) {
                $lastConnectionError = $e;
                Log::warning('Auth remote unavailable', [
                    'auth_base_url' => $authBaseUrl,
                    'path' => $request->path(),
                    'method' => $request->method(),
                    'error' => $e->getMessage(),
                ]);
                error_log('Auth remote unavailable: '.$e->getMessage());
            }
        }

        if (! $response) {
            $payload = [
                'message' => "Service d'authentification indisponible. Veuillez reessayer dans quelques instants.",
            ];
            if (config('app.debug') && $lastConnectionError) {
                $payload['debug'] = $lastConnectionError->getMessage();
            }

            return response()->json($payload, 503);
        }

        if (! $response->successful()) {
            Log::info('Auth token rejected by auth service', [
                'auth_base_url' => $resolvedBaseUrl,
                'status' => $response->status(),
                'path' => $request->path(),
                'method' => $request->method(),
            ]);
            return response()->json(['message' => 'Token invalide ou expiré. Veuillez vous reconnecter.'], 401);
        }

        $user = $response->json();
        if (! is_array($user) || ! isset($user['id'])) {
            return response()->json(['message' => 'Réponse auth invalide.'], 401);
        }

        $authUser = (object) [
            'id' => (int) $user['id'],
            'email' => $user['email'] ?? null,
            'nom' => $user['nom'] ?? null,
            'prenom' => $user['prenom'] ?? null,
            'role' => $user['role'] ?? null,
        ];

        $request->setUserResolver(fn () => $authUser);

        return $next($request);
    }

    private function extractBearerToken(Request $request): ?string
    {
        $header = (string) $request->header('Authorization', '');
        if ($header !== '' && preg_match('/^\s*Bearer\s+(.+)\s*$/i', $header, $matches)) {
            return trim((string) ($matches[1] ?? ''));
        }

        return null;
    }

    /**
     * Construit une liste d'URL candidates pour joindre le service d'auth selon le contexte d'exécution
     * (Docker inter-conteneurs vs exécution locale sur l'hôte).
     *
     * @return list<string>
     */
    private function buildAuthBaseUrls(string $configuredBaseUrl): array
    {
        $candidates = [];
        if ($configuredBaseUrl !== '') {
            $candidates[] = $configuredBaseUrl;
        }

        $isDockerRuntime = file_exists('/.dockerenv');
        if ($isDockerRuntime) {
            $candidates[] = 'http://auth:8080/api';
            $candidates[] = 'http://host.docker.internal:8080/api';
        } else {
            $candidates[] = 'http://localhost:8080/api';
            $candidates[] = 'http://auth:8080/api';
        }

        return array_values(array_unique(array_map(
            fn (string $url): string => rtrim($url, '/'),
            $candidates
        )));
    }
}
