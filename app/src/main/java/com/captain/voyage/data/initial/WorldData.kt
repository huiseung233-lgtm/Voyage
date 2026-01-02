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

    // 1. 아이템 (무역품)
    val items = listOf(
        Item(id = 1, name = "곡물", basePrice = 10, description = "기초 식량입니다.", type = ItemType.FOOD, effectValue = 10),
        Item(id = 2, name = "생선", basePrice = 15, description = "갓 잡은 신선한 생선입니다.", type = ItemType.FOOD, effectValue = 15),
        Item(id = 3, name = "목재", basePrice = 20, description = "건축용 목재입니다.", type = ItemType.TRADE_GOOD),
        Item(id = 4, name = "석재", basePrice = 25, description = "무거운 석재입니다.", type = ItemType.TRADE_GOOD),
        Item(id = 5, name = "철광석", basePrice = 40, description = "제련용 철광석입니다.", type = ItemType.TRADE_GOOD),
        Item(id = 6, name = "소금", basePrice = 50, description = "식품 보존에 필수적입니다.", type = ItemType.TRADE_GOOD),
        Item(id = 7, name = "찻잎", basePrice = 60, description = "향긋한 찻잎입니다.", type = ItemType.TRADE_GOOD),
        Item(id = 8, name = "향신료", basePrice = 100, description = "귀한 향신료입니다.", type = ItemType.TRADE_GOOD),
        Item(id = 9, name = "비단", basePrice = 120, description = "부드럽고 비싼 옷감입니다.", type = ItemType.TRADE_GOOD),
        Item(id = 10, name = "황금", basePrice = 300, description = "매우 가치 있는 금속입니다.", type = ItemType.TRADE_GOOD)
    )

    // 2. 지형 (대륙 및 섬)
    val landmasses = listOf(
        // 서쪽 대륙
        LandShape.Circle(-8000.0, 0.0, 1500.0),
        LandShape.Circle(-8500.0, 1000.0, 1200.0),
        LandShape.Circle(-8500.0, -1000.0, 1200.0),

        // 동쪽 대륙
        LandShape.Circle(8000.0, 0.0, 1500.0),
        LandShape.Circle(8500.0, 1200.0, 1000.0),
        LandShape.Circle(8500.0, -1200.0, 1000.0),

        // 중간 크기 땅
        LandShape.Circle(0.0, 4000.0, 800.0),   
        LandShape.Circle(0.0, -4000.0, 800.0),  
        LandShape.Circle(2000.0, 1000.0, 600.0), 
        LandShape.Circle(-2000.0, -1000.0, 600.0), 

        // 흩어진 작은 섬들
        LandShape.Circle(-4000.0, 6000.0, 300.0),
        LandShape.Circle(4000.0, -6000.0, 300.0),
        LandShape.Circle(6000.0, 6000.0, 350.0),
        LandShape.Circle(-6000.0, -6000.0, 350.0),
        LandShape.Circle(1000.0, 8500.0, 250.0),
        LandShape.Circle(-1000.0, -8500.0, 250.0),
        LandShape.Circle(-9000.0, 9000.0, 400.0), 
        LandShape.Circle(9000.0, -9000.0, 400.0), 
        LandShape.Circle(0.0, 0.0, 200.0) 
    )

    // 3. 항구 (25개)
    val ports = listOf(
        // 서쪽 대륙 항구 (Y>0 is Bottom in Canvas -> South, Y<0 is Top -> North)
        // Westia North (Top) -> Y should be negative. Was 1700 (Bottom). Swap name to South.
        // Requested: Westia "North" (actually at bottom) -> Move SE (X+, Y+). 
        // Let's just fix the names based on visual position (Top=North, Bottom=South).
        
        // Old Westia North (Y=1700, Bottom) -> Rename to Westia South. Move SE -> (-7800, 1900)
        Port(id = 1, name = "웨스티아 남항", posX = -7800.0, posY = 1900.0, hasShipyard = true, hasTavern = true, hasMarket = true),
        
        // Old Westia South (Y=-1700, Top) -> Rename to Westia North. Move NE -> (-7800, -1900)
        Port(id = 2, name = "웨스티아 북항", posX = -7800.0, posY = -1900.0, hasMarket = true),
        
        Port(id = 11, name = "웨스티아 본항", posX = -9550.0, posY = 0.0, hasShipyard = true, hasTavern = true, hasMarket = true),
        Port(id = 12, name = "선인장 항구", posX = -9350.0, posY = 1850.0, hasMarket = true),
        
        // 동쪽 대륙 항구
        Port(id = 3, name = "이스티아 본항", posX = 6450.0, posY = 0.0, hasShipyard = true, hasTavern = true, hasMarket = true),
        
        // Old Eastia South (Y=-1700, Top) -> Rename to Eastia North. Move NW -> (7800, -1900)
        Port(id = 9, name = "이스티아 북항", posX = 7800.0, posY = -1900.0, hasMarket = true),
        
        // Old Eastia North (Y=2250, Bottom) -> Rename to Eastia South.
        Port(id = 4, name = "이스티아 남항", posX = 8500.0, posY = 2250.0, hasMarket = true),
        
        Port(id = 10, name = "강철 항구", posX = 9550.0, posY = -1200.0, hasShipyard = true, hasMarket = true),

        // 중간 지역 항구 (Y>0 Bottom -> South, Y<0 Top -> North)
        Port(id = 5, name = "남부 교차점", posX = 0.0, posY = 3150.0, hasTavern = true, hasMarket = true), 
        Port(id = 6, name = "북부 교차점", posX = 0.0, posY = -3150.0, hasMarket = true), 
        Port(id = 13, name = "중앙 우측 선착장", posX = 1350.0, posY = 1000.0, hasMarket = true), 
        Port(id = 14, name = "중앙 좌측 선착장", posX = -1350.0, posY = -1000.0, hasMarket = true), 
        
        // 외곽 섬 항구
        Port(id = 7, name = "안개 섬", posX = -4000.0, posY = 5650.0, hasMarket = true),
        Port(id = 8, name = "산호 항구", posX = 4000.0, posY = -5650.0, hasMarket = true),
        Port(id = 15, name = "서리 초소", posX = -1000.0, posY = -8200.0, hasTavern = true, hasMarket = true),
        
        // [특수] 가라앉은 암초 - 정착지 건설 가능
        Port(id = 16, name = "가라앉은 암초", posX = 1000.0, posY = 8200.0, hasMarket = true, canEstablishSettlement = true),
        
        Port(id = 17, name = "봉우리 항구", posX = 5600.0, posY = 6000.0, hasMarket = true),
        Port(id = 18, name = "낮은 항구", posX = -5600.0, posY = -6000.0, hasMarket = true),
        Port(id = 19, name = "북서쪽 끝단", posX = -8550.0, posY = 9000.0, hasMarket = true),
        Port(id = 20, name = "남동쪽 끝단", posX = 8550.0, posY = -9000.0, hasMarket = true),
        
        Port(id = 21, name = "고래만", posX = 0.0, posY = 6500.0, hasMarket = true),
        Port(id = 22, name = "거북이 해변", posX = 0.0, posY = -6500.0, hasMarket = true),
        Port(id = 23, name = "상어 지느러미 항", posX = 3500.0, posY = 0.0, hasMarket = true),
        Port(id = 24, name = "돌고래만", posX = -3500.0, posY = 0.0, hasMarket = true),

        Port(id = 25, name = "시작의 항구", posX = 0.0, posY = 250.0, hasShipyard = true, hasTavern = true, hasMarket = true)
    )

    fun isLand(x: Double, y: Double): Boolean {
        return getCollidingLand(x, y) != null
    }

    fun getCollidingLand(x: Double, y: Double): LandShape.Circle? {
        return landmasses.firstOrNull { shape ->
            when (shape) {
                is LandShape.Circle -> {
                    val dx = x - shape.centerX
                    val dy = y - shape.centerY
                    sqrt(dx * dx + dy * dy) <= shape.radius
                }
            }
        } as? LandShape.Circle
    }

    fun getInitialMarkets(): List<Market> {
        val markets = mutableListOf<Market>()
        val rand = Random(123)

        ports.forEach { port ->
            // 지역별 특산품 정의
            val specialties = mutableListOf<Long>()
            val demands = mutableListOf<Long>()

            when {
                // 서쪽 대륙 (광물 풍부)
                port.posX < -5000 -> {
                    specialties.addAll(listOf(4L, 5L, 10L)) // 석재, 철광석, 금
                    demands.addAll(listOf(8L, 9L)) // 향신료, 비단 (사치품 부족)
                }
                // 동쪽 대륙 (사치품 풍부)
                port.posX > 5000 -> {
                    specialties.addAll(listOf(7L, 8L, 9L)) // 찻잎, 향신료, 비단
                    demands.addAll(listOf(5L, 10L)) // 철광석, 금 (광물 부족)
                }
                // 그 외 (식량, 기초 자원)
                else -> {
                    specialties.addAll(listOf(1L, 2L, 3L)) // 곡물, 생선, 목재
                    // 특별한 수요 없음 (평범)
                }
            }

            items.forEach { item ->
                var baseBuyRate = 1.0
                var baseSellRate = 1.0

                if (specialties.contains(item.id)) {
                    // 특산품: 싸게 판다 (0.5 ~ 0.8)
                    baseBuyRate = 0.5 + rand.nextDouble() * 0.3
                    baseSellRate = baseBuyRate * 0.8 // 매입가는 더 낮음
                } else if (demands.contains(item.id)) {
                    // 수요품: 비싸게 판다 (1.3 ~ 1.8) - 사실상 비싸게 사주는 곳
                    baseBuyRate = 1.3 + rand.nextDouble() * 0.5
                    baseSellRate = baseBuyRate * 0.9 
                } else {
                    // 일반: (0.9 ~ 1.1)
                    baseBuyRate = 0.9 + rand.nextDouble() * 0.2
                    baseSellRate = baseBuyRate * 0.8
                }

                // 가격 결정
                val buyPrice = (item.basePrice * baseBuyRate).toInt().coerceAtLeast(1)
                val sellPrice = (item.basePrice * baseSellRate).toInt().coerceAtLeast(1)
                
                // 재고: 특산품은 많고, 수요품은 적음
                val stock = if (specialties.contains(item.id)) rand.nextInt(300) + 100
                            else if (demands.contains(item.id)) rand.nextInt(50)
                            else rand.nextInt(100) + 20

                markets.add(Market(
                    portId = port.id,
                    itemId = item.id,
                    buyPrice = buyPrice,
                    sellPrice = sellPrice,
                    stock = stock
                ))
            }
        }
        return markets
    }
}