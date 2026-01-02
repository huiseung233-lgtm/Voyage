package com.captain.voyage.data.initial

import com.captain.voyage.data.model.Item
import com.captain.voyage.data.model.ItemType
import com.captain.voyage.data.model.Market
import com.captain.voyage.data.model.Port
import java.util.Random
import kotlin.math.sqrt

sealed class LandShape {
    data class Circle(val centerX: Double, val centerY: Double, val radius: Double) : LandShape()
}

object WorldData {

    // 1. Items
    val items = listOf(
        Item(id = 1, name = "Grain", basePrice = 10, description = "Basic food staple.", type = ItemType.FOOD, effectValue = 10),
        Item(id = 2, name = "Fish", basePrice = 15, description = "Freshly caught fish.", type = ItemType.FOOD, effectValue = 15),
        Item(id = 3, name = "Wood", basePrice = 20, description = "Timber for construction.", type = ItemType.TRADE_GOOD),
        Item(id = 4, name = "Stone", basePrice = 25, description = "Heavy stones.", type = ItemType.TRADE_GOOD),
        Item(id = 5, name = "Iron Ore", basePrice = 40, description = "Raw iron.", type = ItemType.TRADE_GOOD),
        Item(id = 6, name = "Salt", basePrice = 50, description = "Preservative.", type = ItemType.TRADE_GOOD),
        Item(id = 7, name = "Tea", basePrice = 60, description = "Aromatic leaves.", type = ItemType.TRADE_GOOD),
        Item(id = 8, name = "Spices", basePrice = 100, description = "Expensive spices.", type = ItemType.TRADE_GOOD),
        Item(id = 9, name = "Silk", basePrice = 120, description = "Fine fabric.", type = ItemType.TRADE_GOOD),
        Item(id = 10, name = "Gold", basePrice = 300, description = "Valuable metal.", type = ItemType.TRADE_GOOD)
    )

    // 2. Topology (Continents and Islands)
    val landmasses = listOf(
        // West Continent (Cluster of circles)
        LandShape.Circle(-8000.0, 0.0, 1500.0),
        LandShape.Circle(-8500.0, 1000.0, 1200.0),
        LandShape.Circle(-8500.0, -1000.0, 1200.0),

        // East Continent (Cluster of circles)
        LandShape.Circle(8000.0, 0.0, 1500.0),
        LandShape.Circle(8500.0, 1200.0, 1000.0),
        LandShape.Circle(8500.0, -1200.0, 1000.0),

        // Mid-size Lands
        LandShape.Circle(0.0, 4000.0, 800.0),   // North Mid
        LandShape.Circle(0.0, -4000.0, 800.0),  // South Mid
        LandShape.Circle(2000.0, 1000.0, 600.0), // Center Right
        LandShape.Circle(-2000.0, -1000.0, 600.0), // Center Left

        // Scattered Small Islands
        LandShape.Circle(-4000.0, 6000.0, 300.0),
        LandShape.Circle(4000.0, -6000.0, 300.0),
        LandShape.Circle(6000.0, 6000.0, 350.0),
        LandShape.Circle(-6000.0, -6000.0, 350.0),
        LandShape.Circle(1000.0, 8500.0, 250.0),
        LandShape.Circle(-1000.0, -8500.0, 250.0),
        LandShape.Circle(-9000.0, 9000.0, 400.0), // Northwest far island
        LandShape.Circle(9000.0, -9000.0, 400.0), // Southeast far island
        LandShape.Circle(0.0, 0.0, 200.0) // Small starter island in the center
    )

    // 3. Ports
    val ports = listOf(
        // West Continent Ports
        Port(id = 1, name = "Westia North", posX = -7500.0, posY = 1700.0, hasShipyard = true, hasTavern = true, hasMarket = true), // Moved out
        Port(id = 2, name = "Westia South", posX = -7500.0, posY = -1700.0, hasMarket = true), // Moved out
        
        // East Continent Ports
        Port(id = 3, name = "Eastia Main", posX = 6300.0, posY = 0.0, hasShipyard = true, hasTavern = true, hasMarket = true), // Moved closer to coast
        Port(id = 4, name = "Eastia North", posX = 7800.0, posY = 2600.0, hasMarket = true), // Moved out

        // Mid-land Ports
        Port(id = 5, name = "North Junction", posX = 0.0, posY = 2500.0, hasTavern = true, hasMarket = true), // Moved out
        Port(id = 6, name = "South Junction", posX = 0.0, posY = -2500.0, hasMarket = true), // Moved out
        
        // Scattered (Fill up to ~25)
        Port(id = 7, name = "Mist Isle", posX = -4000.0, posY = 5200.0, hasMarket = true),
        Port(id = 8, name = "Coral Port", posX = 4000.0, posY = -5200.0, hasMarket = true),
        
        // East Continent Additional
        Port(id = 9, name = "Eastia South", posX = 7800.0, posY = -2300.0, hasMarket = true),
        Port(id = 10, name = "Iron Harbor", posX = 9700.0, posY = 500.0, hasShipyard = true, hasMarket = true),
        
        // West Continent Additional
        Port(id = 11, name = "Westia Harbor", posX = -9700.0, posY = 0.0, hasShipyard = true, hasTavern = true, hasMarket = true),
        Port(id = 12, name = "Cactus Port", posX = -8800.0, posY = 2100.0, hasMarket = true),
        
        // Mid-size Land Ports
        Port(id = 13, name = "Center Right Dock", posX = 2000.0, posY = 1700.0, hasMarket = true),
        Port(id = 14, name = "Center Left Dock", posX = -2000.0, posY = -1700.0, hasMarket = true),
        
        // Scattered Small Island Ports
        Port(id = 15, name = "Frost Outpost", posX = -1000.0, posY = -7900.0, hasTavern = true, hasMarket = true),
        Port(id = 16, name = "Sunken Reef", posX = 1000.0, posY = 7900.0, hasMarket = true),
        Port(id = 17, name = "Peak Port", posX = 6000.0, posY = 5200.0, hasMarket = true),
        Port(id = 18, name = "Low Port", posX = -6000.0, posY = -5800.0, hasMarket = true),
        Port(id = 19, name = "Northwest Point", posX = -8700.0, posY = 8200.0, hasMarket = true),
        Port(id = 20, name = "Southeast Point", posX = 8700.0, posY = -8200.0, hasMarket = true),
        
        // More scattered
        Port(id = 21, name = "Whale Bay", posX = 0.0, posY = 6500.0, hasMarket = true),
        Port(id = 22, name = "Turtle Beach", posX = 0.0, posY = -6500.0, hasMarket = true),
        Port(id = 23, name = "Shark Fin", posX = 3500.0, posY = 0.0, hasMarket = true),
        Port(id = 24, name = "Dolphin Cove", posX = -3500.0, posY = 0.0, hasMarket = true),

        Port(id = 25, name = "Beginner's Bay", posX = 0.0, posY = 400.0, hasShipyard = true, hasTavern = true, hasMarket = true) // Moved further out
    )
    // Note: I will add more ports later if needed to reach exactly 25, but this is the core set.

    fun isLand(x: Double, y: Double): Boolean {
        return landmasses.any { shape ->
            when (shape) {
                is LandShape.Circle -> {
                    val dx = x - shape.centerX
                    val dy = y - shape.centerY
                    sqrt(dx * dx + dy * dy) <= shape.radius
                }
            }
        }
    }

    fun getInitialMarkets(): List<Market> {
        val markets = mutableListOf<Market>()
        val rand = Random(123)
        ports.forEach { port ->
             items.forEach { item ->
                 markets.add(Market(
                     portId = port.id,
                     itemId = item.id,
                     buyPrice = (item.basePrice * (0.8 + rand.nextDouble() * 0.4)).toInt(),
                     sellPrice = (item.basePrice * (0.6 + rand.nextDouble() * 0.4)).toInt(),
                     stock = rand.nextInt(200)
                 ))
             }
        }
        return markets
    }
}
