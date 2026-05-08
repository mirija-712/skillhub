<?php

namespace Tests\Feature;

use App\Models\CategorieFormation;
use App\Models\Formation;
use App\Models\Inscription;
use App\Models\Utilisateur;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Http;
use Tests\TestCase;

class FormateurApprenantsTest extends TestCase
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

    public function test_formateur_proprietaire_gets_apprenants_list_returns_200(): void
    {
        $categorie = CategorieFormation::factory()->create();
        $formateur = Utilisateur::factory()->formateur()->create();
        $formation = Formation::factory()->create([
            'id_formateur' => $formateur->id,
            'id_categorie' => $categorie->id,
        ]);
        $apprenant = Utilisateur::factory()->participant()->create([
            'nom' => 'Apprenant Test',
            'email' => 'apprenant.test@example.com',
        ]);
        Inscription::create([
            'utilisateur_id' => $apprenant->id,
            'formation_id' => $formation->id,
            'progression' => 45,
        ]);

        $response = $this->withRemoteAuthAs($formateur->id, 'formateur', $formateur->email)
            ->getJson("/api/formateur/{$formation->id}/apprenants");

        $response->assertStatus(200)
            ->assertJsonPath('apprenants.0.id', $apprenant->id)
            ->assertJsonPath('apprenants.0.nom', 'Apprenant Test')
            ->assertJsonPath('apprenants.0.email', 'apprenant.test@example.com')
            ->assertJsonPath('apprenants.0.progression', 45);
    }

    public function test_formateur_non_proprietaire_gets_403(): void
    {
        $categorie = CategorieFormation::factory()->create();
        $owner = Utilisateur::factory()->formateur()->create();
        $otherFormateur = Utilisateur::factory()->formateur()->create();
        $formation = Formation::factory()->create([
            'id_formateur' => $owner->id,
            'id_categorie' => $categorie->id,
        ]);

        $response = $this->withRemoteAuthAs($otherFormateur->id, 'formateur', $otherFormateur->email)
            ->getJson("/api/formateur/{$formation->id}/apprenants");

        $response->assertStatus(403);
    }

    public function test_formateur_without_apprenants_gets_200_with_empty_array(): void
    {
        $categorie = CategorieFormation::factory()->create();
        $formateur = Utilisateur::factory()->formateur()->create();
        $formation = Formation::factory()->create([
            'id_formateur' => $formateur->id,
            'id_categorie' => $categorie->id,
        ]);

        $response = $this->withRemoteAuthAs($formateur->id, 'formateur', $formateur->email)
            ->getJson("/api/formateur/{$formation->id}/apprenants");

        $response->assertStatus(200)
            ->assertJson(['apprenants' => []]);
    }

    public function test_get_apprenants_without_token_returns_401(): void
    {
        $formation = Formation::factory()->create();

        $response = $this->getJson("/api/formateur/{$formation->id}/apprenants");

        $response->assertStatus(401);
    }
}
