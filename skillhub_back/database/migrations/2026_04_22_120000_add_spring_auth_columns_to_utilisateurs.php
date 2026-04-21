<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

/**
 * Bases déjà migrées avec l’ancienne définition minimale de utilisateurs :
 * ajoute les colonnes attendues par authentification_back sans toucher au reste.
 */
return new class extends Migration
{
    public function up(): void
    {
        if (!Schema::hasTable('utilisateurs')) {
            return;
        }

        if (!Schema::hasColumn('utilisateurs', 'token')) {
            Schema::table('utilisateurs', function (Blueprint $table) {
                $table->string('token', 64)->nullable()->unique();
            });
        }
        if (!Schema::hasColumn('utilisateurs', 'failed_login_attempts')) {
            Schema::table('utilisateurs', function (Blueprint $table) {
                $table->unsignedInteger('failed_login_attempts')->default(0);
            });
        }
        if (!Schema::hasColumn('utilisateurs', 'lock_until')) {
            Schema::table('utilisateurs', function (Blueprint $table) {
                $table->timestamp('lock_until', 6)->nullable();
            });
        }
    }

    public function down(): void
    {
        if (!Schema::hasTable('utilisateurs')) {
            return;
        }

        Schema::table('utilisateurs', function (Blueprint $table) {
            if (Schema::hasColumn('utilisateurs', 'lock_until')) {
                $table->dropColumn('lock_until');
            }
            if (Schema::hasColumn('utilisateurs', 'failed_login_attempts')) {
                $table->dropColumn('failed_login_attempts');
            }
            if (Schema::hasColumn('utilisateurs', 'token')) {
                $table->dropColumn('token');
            }
        });
    }
};
