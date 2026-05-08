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
        $user = $request->user();
        if (! $user) {
            return response()->json(['message' => 'Token manquant ou invalide. Veuillez vous reconnecter.'], 401);
        }

        $formation = Formation::find($id);
        if (! $formation) {
            return response()->json(['message' => 'Formation introuvable'], 404);
        }

        $isInscrit = Inscription::where('utilisateur_id', (int) $user->id)
            ->where('formation_id', $formation->id)
            ->exists();

        if (! $isInscrit) {
            return response()->json(['message' => 'Vous devez être inscrit à cette formation pour la noter.'], 403);
        }

        $validator = Validator::make($request->all(), [
            'note' => ['required', 'integer', 'between:1,5'],
            'commentaire' => ['required', 'string'],
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Note invalide.',
                'erreurs' => $validator->errors(),
            ], 400);
        }

        $alreadyRated = Rating::where('user_id', (int) $user->id)
            ->where('formation_id', $formation->id)
            ->exists();

        if ($alreadyRated) {
            return response()->json(['message' => 'Vous avez déjà noté cette formation.'], 400);
        }

        $rating = Rating::create([
            'user_id' => (int) $user->id,
            'formation_id' => $formation->id,
            'note' => (int) $request->input('note'),
            'commentaire' => (string) $request->input('commentaire'),
        ]);

        return response()->json(['rating' => $rating], 201);
    }
}
