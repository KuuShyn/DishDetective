package com.thesis.dishdetective_xml

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://bkfgwjuejajafghyjozx.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJrZmd3anVlamFqYWZnaHlqb3p4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTc4MzU2NjUsImV4cCI6MjAzMzQxMTY2NX0.gSm2x5N_gSXrLIauV3Q8pXwWUgzPNz90jV9J1JSId_s"
    ) {
        install(Postgrest)
    }

}
