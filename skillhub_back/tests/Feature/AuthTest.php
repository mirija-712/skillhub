<?php

namespace Tests\Feature;

use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Http;
use Tests\TestCase;

class AuthTest extends TestCase
{
    use RefreshDatabase;

    public function test_protected_route_without_token_returns_401(): void
    {
        $response = $this->postJson('/api/formations', []);
        $response->assertStatus(401);
    }

    public function test_protected_route_with_invalid_remote_token_returns_401(): void
    {
        Http::fake([
            '*' => Http::response(['message' => 'Unauthorized'], 401),
        ]);

        $response = $this->withHeader('Authorization', 'Bearer invalid-token')
            ->postJson('/api/formations', []);

        $response->assertStatus(401);
    }

    public function test_formateur_can_access_formateur_protected_route(): void
    {
        Http::fake([
            '*' => Http::response([
                'id' => 10,
                'email' => 'formateur@test.com',
                'role' => 'formateur',
            ], 200),
        ]);

        $response = $this->withHeader('Authorization', 'Bearer valid-token')
            ->postJson('/api/formations', [
                'title' => '',
                'description' => '',
                'price' => -1,
                'duration' => -1,
                'level' => 'invalid',
            ]);

        // Le middleware auth.remote + formateur laisse bien passer; on tombe ensuite sur la validation métier.
        $response->assertStatus(422);
    }

    public function test_participant_cannot_access_formateur_route_returns_403(): void
    {
        Http::fake([
            '*' => Http::response([
                'id' => 11,
                'email' => 'apprenant@test.com',
                'role' => 'participant',
            ], 200),
        ]);

        $response = $this->withHeader('Authorization', 'Bearer participant-token')
            ->postJson('/api/formations', []);

        $response->assertStatus(403);
    }
}
