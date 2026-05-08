<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Formation;
use App\Models\Inscription;
use App\Models\Rating;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class RatingController extends Controller
{
    public function store(Request $request, int $id): JsonResponse
    {
        $status = 201;
        $payload = [];
        $user = $request->user();
        if (! $user) {
            $status = 401;
            $payload = ['message' => 'Token manquant ou invalide. Veuillez vous reconnecter.'];
        } else {
            $formation = Formation::find($id);
            if (! $formation) {
                $status = 404;
                $payload = ['message' => 'Formation introuvable'];
            } else {
                $isInscrit = Inscription::where('utilisateur_id', (int) $user->id)
                    ->where('formation_id', $formation->id)
                    ->exists();

                if (! $isInscrit) {
                    $status = 403;
                    $payload = ['message' => 'Vous devez être inscrit à cette formation pour la noter.'];
                } else {
                    $validator = Validator::make($request->all(), [
                        'note' => ['required', 'integer', 'between:1,5'],
                        'commentaire' => ['required', 'string'],
                    ]);

                    if ($validator->fails()) {
                        $status = 400;
                        $payload = [
                            'message' => 'Note invalide.',
                            'erreurs' => $validator->errors(),
                        ];
                    } else {
                        $alreadyRated = Rating::where('user_id', (int) $user->id)
                            ->where('formation_id', $formation->id)
                            ->exists();

                        if ($alreadyRated) {
                            $status = 400;
                            $payload = ['message' => 'Vous avez déjà noté cette formation.'];
                        } else {
                            $rating = Rating::create([
                                'user_id' => (int) $user->id,
                                'formation_id' => $formation->id,
                                'note' => (int) $request->input('note'),
                                'commentaire' => (string) $request->input('commentaire'),
                            ]);
                            $payload = ['rating' => $rating];
                        }
                    }
                }
            }
        }
        return response()->json($payload, $status);
    }
}
