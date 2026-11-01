package com.sizzlesense.ai.domain.model.session

enum class LearnerArchetypeOption(
    val id: String,
    val title: String,
    val jdSubtitle: String,
    val presetReflections: Map<Int, String>
) {
    MOISTURE_BLINDNESS(
        id = "moisture_blindness",
        title = "Learner A: Moisture Blindness",
        jdSubtitle = "Crowds pan & stirs in liquid without evaporating water",
        presetReflections = mapOf(
            1 to "I kept stirring the onions for 8 minutes on medium heat, but they stayed pale and watery instead of browning.",
            2 to "I spread the onions out and waited until the wet bubbling sound turned into a crisp sizzle before stirring!",
            3 to "As soon as I heard the sizzle sharpen after moisture cooked off, I adjusted heat to keep steady golden browning."
        )
    ),
    HEAT_ANXIETY(
        id = "heat_anxiety",
        title = "Learner B: Heat Anxiety",
        jdSubtitle = "Fears burning when steam rises & dials heat too low",
        presetReflections = mapOf(
            1 to "It started steaming a lot right away so I turned the heat way down so it wouldn't burn, and nothing browned.",
            2 to "I saw the white steam again, recognized it was harmless water vapor, and kept the heat steady until golden.",
            3 to "I used the steady applause sizzle sound and clear oil shimmer to ride medium-high heat confidently."
        )
    ),
    PERCEPTION_BLINDSPOT(
        id = "perception_blindspot",
        title = "Learner C: Perception Blindspot",
        jdSubtitle = "Believes soft pale onions are browned after 5-min timer",
        presetReflections = mapOf(
            1 to "I sautéed the onions for 5 minutes just like the recipe said and they browned really nicely!",
            2 to "I ignored the 5-minute timer this time and waited until I saw actual amber-gold color on the edges and pan fond.",
            3 to "Now I can immediately tell the difference between sweaty translucent onions and true nutty golden browning."
        )
    )
}
