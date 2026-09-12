package com.mamafit.app

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Single shared Supabase client for the app.
 *
 * Credentials are injected at build time from local.properties (which is
 * gitignored), so they never end up in version control. See the
 * SUPABASE_URL / SUPABASE_ANON_KEY entries in app/build.gradle.kts.
 */
object SupabaseManager {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}
