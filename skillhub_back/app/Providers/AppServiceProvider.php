<?php

namespace App\Providers;

use Illuminate\Support\ServiceProvider;
use Illuminate\Support\Facades\Schema;

class AppServiceProvider extends ServiceProvider
{
    public function register(): void
    {
        // Intentionally left empty: no container bindings are required yet.
    }

    public function boot(): void
    {
        Schema::defaultStringLength(191);
    }
}