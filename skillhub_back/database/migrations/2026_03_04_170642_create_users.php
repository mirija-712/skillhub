<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Table utilisateurs partagée avec authentification_back (JPA).
     * Colonnes alignées sur l’entité JPA User d’authentification_back (token, lockout, etc.).
     * Aucune création automatique côté Spring : le schéma vient des migrations Laravel.
     */
    public function up(): void
    {
        Schema::create('utilisateurs', function (Blueprint $table) {
            $table->id();
            $table->string('email', 100)->unique();
            $table->string('mot_de_passe', 255);
            $table->string('nom', 100);
            $table->string('prenom', 100)->nullable();
            $table->enum('role', ['participant', 'formateur'])->default('participant');
            $table->string('token', 64)->nullable()->unique();
            $table->unsignedInteger('failed_login_attempts')->default(0);
            $table->timestamp('lock_until', 6)->nullable();
            $table->timestamps(6);
        });
    }

    /**
     * Suppression de la table utilisateurs.
     */
    public function down(): void
    {
        Schema::dropIfExists('utilisateurs');
    }
};
