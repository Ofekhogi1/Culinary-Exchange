package com.college.culinaryexchange.util

import android.util.Log
import com.college.culinaryexchange.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

object SeedDataUtil {

    private const val TAG = "SeedDataUtil"
    private val db = FirebaseFirestore.getInstance()
    private val posts = db.collection("posts")

    suspend fun seedIfEmpty() {
        val user = FirebaseAuth.getInstance().currentUser
        Log.d(TAG, "seedIfEmpty: currentUser=${user?.uid ?: "NULL - not authenticated!"}")
        try {
            val existing = posts.get().await()
            val count = existing.size()
            Log.d(TAG, "seedIfEmpty: existing post count=$count")
            if (count >= 8) {
                Log.d(TAG, "seedIfEmpty: already fully seeded, skipping")
                return
            }
            // Delete any partial seed data before re-seeding
            if (count > 0) {
                Log.d(TAG, "seedIfEmpty: deleting $count partial posts and re-seeding")
                existing.documents.forEach { it.reference.delete().await() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "seedIfEmpty: FAILED to read/clear posts: ${e::class.simpleName}: ${e.message}", e)
            throw e
        }

        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val day = 86_400_000L

        val samplePosts = listOf(
            Post(
                id = UUID.randomUUID().toString(),
                userId = "seed_marco",
                userName = "Chef Marco",
                userAvatarUrl = "",
                title = "Creamy Spanish Paella",
                description = "A rich and aromatic seafood paella straight from the Valencia region. Packed with saffron-infused rice, tiger prawns, and fresh parsley — this dish brings the Mediterranean to your table.",
                imageUrl = "https://www.themealdb.com/images/media/meals/9bl20p1763248192.jpg",
                timestamp = now - 2 * hour,
                ingredients = listOf(
                    "500g paella rice", "400g tiger prawns (peeled)",
                    "4 tbsp olive oil", "1 pinch saffron threads",
                    "1L chicken stock", "1 onion (diced)",
                    "3 garlic cloves", "1 red pepper", "Fresh parsley", "1 lemon"
                ),
                instructions = listOf(
                    "Heat olive oil in a large paella pan over medium heat.",
                    "Sauté onion, garlic, and red pepper for 5 minutes until soft.",
                    "Add paella rice and stir to coat with oil.",
                    "Dissolve saffron in warm stock and pour over the rice.",
                    "Simmer uncovered for 15 minutes without stirring.",
                    "Arrange prawns on top and cook until pink, about 5 minutes.",
                    "Rest for 5 minutes, garnish with parsley and lemon wedges."
                ),
                prepTime = 45,
                servings = 4,
                category = "Seafood",
                nutritionCalories = 520,
                nutritionProtein = "34g",
                nutritionCarbs = "62g",
                nutritionFat = "11g"
            ),
            Post(
                id = UUID.randomUUID().toString(),
                userId = "seed_tokyo",
                userName = "SushiMaster_Tokyo",
                userAvatarUrl = "",
                title = "Classic Salmon Sushi Rolls",
                description = "Perfectly seasoned sushi rice wrapped with fresh salmon, creamy avocado and crisp cucumber. A Japanese classic that's easier to make at home than you think!",
                imageUrl = "https://www.themealdb.com/images/media/meals/g046bb1663960946.jpg",
                timestamp = now - 5 * hour,
                ingredients = listOf(
                    "2 cups sushi rice", "3 tbsp rice wine vinegar",
                    "1 tbsp sugar", "1 tsp salt",
                    "4 nori sheets", "200g fresh salmon (sliced)",
                    "1 avocado", "1 cucumber", "Soy sauce", "Pickled ginger", "Wasabi"
                ),
                instructions = listOf(
                    "Cook sushi rice according to packet instructions.",
                    "Mix vinegar, sugar and salt; fold into warm rice and cool.",
                    "Place nori on a bamboo mat, spread rice leaving 2cm at the top.",
                    "Lay salmon, avocado and cucumber strips across the centre.",
                    "Roll tightly using the mat, pressing firmly.",
                    "Slice into 8 pieces with a wet knife.",
                    "Serve with soy sauce, wasabi and pickled ginger."
                ),
                prepTime = 60,
                servings = 2,
                category = "Seafood",
                nutritionCalories = 380,
                nutritionProtein = "22g",
                nutritionCarbs = "55g",
                nutritionFat = "9g"
            ),
            Post(
                id = UUID.randomUUID().toString(),
                userId = "seed_canada",
                userName = "FoodieCanada",
                userAvatarUrl = "",
                title = "Canadian Poutine",
                description = "Quebec's most beloved comfort food — crispy golden fries loaded with cheese curds and smothered in a rich, silky beef gravy. One bite and you'll be hooked.",
                imageUrl = "https://www.themealdb.com/images/media/meals/uuyrrx1487327597.jpg",
                timestamp = now - day,
                ingredients = listOf(
                    "1kg russet potatoes", "500ml beef gravy",
                    "250g fresh cheese curds", "Vegetable oil for frying",
                    "Salt and pepper", "1 tbsp cornstarch (optional for thicker gravy)"
                ),
                instructions = listOf(
                    "Peel and cut potatoes into thick fries; soak in cold water 30 min.",
                    "Pat dry and fry at 160°C for 5 minutes (blanch); drain.",
                    "Heat gravy in a saucepan; thicken with cornstarch if needed.",
                    "Fry potatoes again at 190°C until golden and crispy.",
                    "Drain on paper towels and season generously with salt.",
                    "Place fries in a bowl, scatter cheese curds on top.",
                    "Pour hot gravy over everything so the curds begin to melt. Serve immediately."
                ),
                prepTime = 35,
                servings = 2,
                category = "Comfort Food",
                nutritionCalories = 740,
                nutritionProtein = "28g",
                nutritionCarbs = "85g",
                nutritionFat = "32g"
            ),
            Post(
                id = UUID.randomUUID().toString(),
                userId = "seed_slavic",
                userName = "SlavicKitchen",
                userAvatarUrl = "",
                title = "Traditional Ukrainian Borscht",
                description = "A hearty, vibrant beetroot soup that has warmed families for centuries. Deep red, full of vegetables and tender beef, served with a dollop of sour cream and fresh dill.",
                imageUrl = "https://www.themealdb.com/images/media/meals/804v1j1764367088.jpg",
                timestamp = now - 2 * day,
                ingredients = listOf(
                    "400g beef short ribs", "3 medium beets (grated)",
                    "1/2 head cabbage (shredded)", "3 potatoes (diced)",
                    "2 carrots (grated)", "1 onion", "3 garlic cloves",
                    "2 tbsp tomato paste", "2L beef stock",
                    "Sour cream", "Fresh dill", "Bay leaves", "Salt & pepper"
                ),
                instructions = listOf(
                    "Simmer beef ribs in stock with bay leaves for 1.5 hours until tender.",
                    "Remove beef, shred meat and discard bones.",
                    "Sauté onion and carrot until soft; add beets and cook 10 minutes.",
                    "Stir in tomato paste and cook 2 more minutes.",
                    "Add potatoes and cabbage to the stock; cook 15 minutes.",
                    "Add the beet mixture and shredded beef; simmer 10 minutes.",
                    "Adjust seasoning and serve topped with sour cream and dill."
                ),
                prepTime = 110,
                servings = 6,
                category = "Soup",
                nutritionCalories = 290,
                nutritionProtein = "21g",
                nutritionCarbs = "28g",
                nutritionFat = "10g"
            ),
            Post(
                id = UUID.randomUUID().toString(),
                userId = "seed_paul",
                userName = "PâtissierPaul",
                userAvatarUrl = "",
                title = "Classic French Flan Pâtissier",
                description = "A silky-smooth custard tart with a golden pastry crust and a beautifully caramelised top. This French bakery staple is simpler than it looks and absolutely irresistible.",
                imageUrl = "https://www.themealdb.com/images/media/meals/0s80wo1764374393.jpg",
                timestamp = now - 3 * day,
                ingredients = listOf(
                    "1 sheet shortcrust pastry", "500ml whole milk",
                    "200ml heavy cream", "4 egg yolks",
                    "120g caster sugar", "60g cornstarch", "1 tsp vanilla extract"
                ),
                instructions = listOf(
                    "Line a 24cm tart tin with pastry; blind bake at 180°C for 15 min.",
                    "Whisk egg yolks, sugar, cornstarch and vanilla until pale.",
                    "Heat milk and cream together until just simmering.",
                    "Slowly pour hot milk into egg mixture, whisking constantly.",
                    "Return to pan and cook over medium heat until very thick.",
                    "Pour into tart shell and smooth the top.",
                    "Bake at 200°C for 25 minutes until deeply golden. Cool completely before slicing."
                ),
                prepTime = 55,
                servings = 8,
                category = "Dessert",
                nutritionCalories = 310,
                nutritionProtein = "6g",
                nutritionCarbs = "38g",
                nutritionFat = "15g"
            ),
            Post(
                id = UUID.randomUUID().toString(),
                userId = "seed_khalid",
                userName = "ArabianKitchen",
                userAvatarUrl = "",
                title = "Saudi Chicken Kabsa",
                description = "The national dish of Saudi Arabia — fragrant long-grain rice cooked in a spiced tomato broth with fall-off-the-bone chicken, topped with raisins, almonds and crispy onions.",
                imageUrl = "https://www.themealdb.com/images/media/meals/utqnjv1763598650.jpg",
                timestamp = now - 4 * day,
                ingredients = listOf(
                    "1 whole chicken (cut into pieces)", "2 cups long-grain rice",
                    "1 can crushed tomatoes", "1 onion", "4 garlic cloves",
                    "2 tsp kabsa spice mix", "1 tsp cinnamon", "1 tsp cumin",
                    "4 cardamom pods", "Handful raisins", "Handful toasted almonds",
                    "Fresh coriander", "Salt and pepper"
                ),
                instructions = listOf(
                    "Fry onions in a large pot until golden; add garlic and spices.",
                    "Add chicken pieces and brown on all sides.",
                    "Pour in crushed tomatoes and enough water to cover; simmer 40 minutes.",
                    "Remove chicken and strain the broth.",
                    "Cook rice in the spiced broth for 18 minutes until tender.",
                    "Grill or broil chicken pieces for 5 minutes to crisp the skin.",
                    "Serve rice on a platter topped with chicken, raisins, almonds and coriander."
                ),
                prepTime = 75,
                servings = 5,
                category = "Chicken",
                nutritionCalories = 620,
                nutritionProtein = "45g",
                nutritionCarbs = "68g",
                nutritionFat = "16g"
            ),
            Post(
                id = UUID.randomUUID().toString(),
                userId = "seed_grillmaster",
                userName = "GrillMaster_BA",
                userAvatarUrl = "",
                title = "Argentine Asado (BBQ)",
                description = "Argentina's legendary barbecue — slow-cooked beef short ribs and chorizo over wood embers. No sauces needed; just perfectly salted meat and the magic of fire.",
                imageUrl = "https://www.themealdb.com/images/media/meals/kgfh3q1763075438.jpg",
                timestamp = now - 5 * day,
                ingredients = listOf(
                    "1.5kg beef short ribs", "400g chorizo sausages",
                    "200g morcilla (blood sausage)", "Coarse salt",
                    "Chimichurri: parsley, garlic, olive oil, red wine vinegar, oregano, chili flakes"
                ),
                instructions = listOf(
                    "Light hardwood or charcoal; wait until coals are white-hot with no flames.",
                    "Season ribs generously with coarse salt only.",
                    "Place ribs bone-side down on the grill 30cm from coals.",
                    "Cook low and slow for 1.5–2 hours, turning once.",
                    "Add chorizo and morcilla in the last 20 minutes.",
                    "Rest meat for 10 minutes before cutting.",
                    "Blend chimichurri ingredients and serve alongside."
                ),
                prepTime = 130,
                servings = 6,
                category = "Beef",
                nutritionCalories = 810,
                nutritionProtein = "58g",
                nutritionCarbs = "2g",
                nutritionFat = "63g"
            ),
            Post(
                id = UUID.randomUUID().toString(),
                userId = "seed_istanbul",
                userName = "StreetFood_Istanbul",
                userAvatarUrl = "",
                title = "Turkish Kumpir (Stuffed Baked Potato)",
                description = "Istanbul's most famous street food — a giant baked potato split open, mashed with butter and cheese, then piled high with toppings of your choice. Filling, fun and totally customisable.",
                imageUrl = "https://www.themealdb.com/images/media/meals/mlchx21564916997.jpg",
                timestamp = now - 6 * day,
                ingredients = listOf(
                    "4 large russet potatoes", "4 tbsp butter",
                    "100g grated kashar (or cheddar) cheese",
                    "Toppings: corn, olives, pickles, sautéed mushrooms, coleslaw, sausage slices, hot sauce"
                ),
                instructions = listOf(
                    "Scrub potatoes, rub with oil and salt, bake at 200°C for 60–70 minutes.",
                    "Test with a skewer — should glide through with no resistance.",
                    "Cut a cross in the top and push the sides in to open.",
                    "Immediately add butter and cheese; mash into the potato flesh with a fork.",
                    "Pile on toppings of your choice — the more the better!",
                    "Serve hot with extra hot sauce on the side."
                ),
                prepTime = 75,
                servings = 1,
                category = "Vegetarian",
                nutritionCalories = 680,
                nutritionProtein = "18g",
                nutritionCarbs = "92g",
                nutritionFat = "28g"
            )
        )

        Log.d(TAG, "seedIfEmpty: writing ${samplePosts.size} posts to Firestore...")
        samplePosts.forEach { post ->
            try {
                posts.document(post.id).set(post).await()
                Log.d(TAG, "seedIfEmpty: wrote '${post.title}'")
            } catch (e: Exception) {
                Log.e(TAG, "seedIfEmpty: FAILED to write '${post.title}': ${e.message}", e)
                throw e
            }
        }
        Log.d(TAG, "seedIfEmpty: all posts written successfully")
    }
}
