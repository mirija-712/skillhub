<?php

namespace Tests\Feature;

use App\Models\CategorieFormation;
use App\Models\Formation;
use App\Models\Inscription;
use App\Models\Rating;
use App\Models\Utilisateur;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Http;
use Tests\TestCase;

class RatingControllerTest extends TestCase
{
    use RefreshDatabase;

    private function withRemoteAuthAs(int $userId, string $role, ?string $email = null): self
    {
        Http::fake([
            '*' => Http::response([
                'id' => $userId,
                'email' => $email ?? "user{$userId}@test.com",
                'role' => $role,
            ], 200),
        ]);

        return $this->withHeader('Authorization', 'Bearer test-token');
    }

    public function test_apprenant_inscrit_can_submit_valid_rating_returns_201(): void
    {
        $categorie = CategorieFormation::factory()->create();
        $formateur = Utilisateur::factory()->formateur()->create();
        $apprenant = Utilisateur::factory()->participant()->create();
        $formation = Formation::factory()->create([
            'id_formateur' => $formateur->id,
            'id_categorie' => $categorie->id,
        ]);
        Inscription::create([
            'utilisateur_id' => $apprenant->id,
            'formation_id' => $formation->id,
            'progression' => 0,
        ]);

        $response = $this->withRemoteAuthAs($apprenant->id, 'participant', $apprenant->email)
            ->postJson("/api/formations/{$formation->id}/noter", [
                'note' => 4,
                'commentaire' => "Très bonne formation, j'ai beaucoup appris !",
            ]);

        $response->assertStatus(201)
            ->assertJsonPath('rating.note', 4);

        $this->assertDatabaseHas('ratings', [
            'user_id' => $apprenant->id,
            'formation_id' => $formation->id,
            'note' => 4,
        ]);
    }

    public function test_same_apprenant_cannot_rate_same_formation_twice_returns_400(): void
    {
        $categorie = CategorieFormation::factory()->create();
        $formateur = Utilisateur::factory()->formateur()->create();
        $apprenant = Utilisateur::factory()->participant()->create();
        $formation = Formation::factory()->create([
            'id_formateur' => $formateur->id,
            'id_categorie' => $categorie->id,
        ]);
        Inscription::create([
            'utilisateur_id' => $apprenant->id,
            'formation_id' => $formation->id,
            'progression' => 0,
        ]);
        Rating::create([
            'user_id' => $apprenant->id,
            'formation_id' => $formation->id,
            'note' => 5,
            'commentaire' => 'Top',
        ]);

        $response = $this->withRemoteAuthAs($apprenant->id, 'participant', $apprenant->email)
            ->postJson("/api/formations/{$formation->id}/noter", [
                'note' => 3,
                'commentaire' => 'Deuxième avis',
            ]);

        $response->assertStatus(400);
    }

    public function test_rating_outside_1_to_5_returns_400(): void
    {
        $categorie = CategorieFormation::factory()->create();
        $formateur = Utilisateur::factory()->formateur()->create();
        $apprenant = Utilisateur::factory()->participant()->create();
        $formation = Formation::factory()->create([
            'id_formateur' => $formateur->id,
            'id_categorie' => $categorie->id,
        ]);
        Inscription::create([
            'utilisateur_id' => $apprenant->id,
            'formation_id' => $formation->id,
            'progression' => 0,
        ]);

        $response = $this->withRemoteAuthAs($apprenant->id, 'participant', $apprenant->email)
            ->postJson("/api/formations/{$formation->id}/noter", [
                'note' => 6,
                'commentaire' => 'Invalide',
            ]);

        $response->assertStatus(400);
    }

    public function test_non_inscrit_apprenant_cannot_rate_returns_403(): void
    {
        $categorie = CategorieFormation::factory()->create();
        $formateur = Utilisateur::factory()->formateur()->create();
        $apprenant = Utilisateur::factory()->participant()->create();
        $formation = Formation::factory()->create([
            'id_formateur' => $formateur->id,
            'id_categorie' => $categorie->id,
        ]);

        $response = $this->withRemoteAuthAs($apprenant->id, 'participant', $apprenant->email)
            ->postJson("/api/formations/{$formation->id}/noter", [
                'note' => 4,
                'commentaire' => 'Avis',
            ]);

        $response->assertStatus(403);
    }

    public function test_request_without_token_returns_401(): void
    {
        $formation = Formation::factory()->create();

        $response = $this->postJson("/api/formations/{$formation->id}/noter", [
            'note' => 4,
            'commentaire' => 'Avis',
        ]);

        $response->assertStatus(401);
    }

    public function test_rating_unknown_formation_returns_404(): void
    {
        $apprenant = Utilisateur::factory()->participant()->create();

        $response = $this->withRemoteAuthAs($apprenant->id, 'participant', $apprenant->email)
            ->postJson('/api/formations/999999/noter', [
                'note' => 4,
                'commentaire' => 'Avis',
            ]);

        $response->assertStatus(404)
            ->assertJsonPath('message', 'Formation introuvable');
    }

    public function test_rating_with_missing_commentaire_returns_400_and_errors(): void
    {
        $categorie = CategorieFormation::factory()->create();
        $formateur = Utilisateur::factory()->formateur()->create();
        $apprenant = Utilisateur::factory()->participant()->create();
        $formation = Formation::factory()->create([
            'id_formateur' => $formateur->id,
            'id_categorie' => $categorie->id,
        ]);
        Inscription::create([
            'utilisateur_id' => $apprenant->id,
            'formation_id' => $formation->id,
            'progression' => 0,
        ]);

        $response = $this->withRemoteAuthAs($apprenant->id, 'participant', $apprenant->email)
            ->postJson("/api/formations/{$formation->id}/noter", [
                'note' => 4,
            ]);

        $response->assertStatus(400)
            ->assertJsonPath('message', 'Note invalide.')
            ->assertJsonStructure(['erreurs' => ['commentaire']]);
    }

    public function test_rating_with_missing_note_returns_400_and_errors(): void
    {
        $categorie = CategorieFormation::factory()->create();
        $formateur = Utilisateur::factory()->formateur()->create();
        $apprenant = Utilisateur::factory()->participant()->create();
        $formation = Formation::factory()->create([
            'id_formateur' => $formateur->id,
            'id_categorie' => $categorie->id,
        ]);
        Inscription::create([
            'utilisateur_id' => $apprenant->id,
            'formation_id' => $formation->id,
            'progression' => 0,
        ]);

        $response = $this->withRemoteAuthAs($apprenant->id, 'participant', $apprenant->email)
            ->postJson("/api/formations/{$formation->id}/noter", [
                'commentaire' => 'Pas de note',
            ]);

        $response->assertStatus(400)
            ->assertJsonPath('message', 'Note invalide.')
            ->assertJsonStructure(['erreurs' => ['note']]);
    }

    public function test_rating_with_non_integer_note_returns_400(): void
    {
        $categorie = CategorieFormation::factory()->create();
        $formateur = Utilisateur::factory()->formateur()->create();
        $apprenant = Utilisateur::factory()->participant()->create();
        $formation = Formation::factory()->create([
            'id_formateur' => $formateur->id,
            'id_categorie' => $categorie->id,
        ]);
        Inscription::create([
            'utilisateur_id' => $apprenant->id,
            'formation_id' => $formation->id,
            'progression' => 0,
        ]);

        $response = $this->withRemoteAuthAs($apprenant->id, 'participant', $apprenant->email)
            ->postJson("/api/formations/{$formation->id}/noter", [
                'note' => 'quatre',
                'commentaire' => 'Type invalide',
            ]);

        $response->assertStatus(400)
            ->assertJsonPath('message', 'Note invalide.');
    }

    public function test_rating_controller_returns_401_when_middlewares_are_disabled_and_user_missing(): void
    {
        $this->withoutMiddleware();
        $formation = Formation::factory()->create();

        $response = $this->postJson("/api/formations/{$formation->id}/noter", [
            'note' => 4,
            'commentaire' => 'Avis',
        ]);

        $response->assertStatus(401)
            ->assertJsonPath('message', 'Token manquant ou invalide. Veuillez vous reconnecter.');
    }
}
