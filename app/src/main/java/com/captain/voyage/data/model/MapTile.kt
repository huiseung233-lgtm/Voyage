package com.captain.voyage.data.model

enum class TileType {
    SEA,
    LAND
}

enum class BiomeType {
    // Sea Biomes
    CALM_SEA,
    STORMY,
    POLAR, // Ice
    TROPICAL, // Coral

    // Land Biomes
    GRASSLAND, // Standard
    DESERT,    // Sand
    SNOW,      // Ice land
    FOREST,    // Jungle
    MOUNTAIN   // Rocks
}

data class MapTile(
    val x: Int,
    val y: Int,
    val type: TileType,
    val biome: BiomeType
)
