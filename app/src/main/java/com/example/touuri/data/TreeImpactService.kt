package com.example.touuri.data

import kotlin.math.PI
import kotlin.math.pow

class TreeImpactService {
    fun calculateImpact(
        species: String,
        height: Double?,
        diameter: Double?,
        age: Int?
    ): TreeImpact {
        val carbonSequestration = calculateCarbonSequestration(height, diameter, age)
        val oxygenProduction = calculateOxygenProduction(height, diameter)
        val stormwaterReduction = calculateStormwaterReduction(diameter)
        val energySavings = calculateEnergySavings(height, diameter)
        val propertyValueIncrease = calculatePropertyValueIncrease(species, height, diameter)

        return TreeImpact(
            carbonSequestration = carbonSequestration,
            oxygenProduction = oxygenProduction,
            stormwaterReduction = stormwaterReduction,
            energySavings = energySavings,
            propertyValueIncrease = propertyValueIncrease,
            recommendations = generateImpactRecommendations(
                carbonSequestration,
                oxygenProduction,
                stormwaterReduction,
                energySavings,
                propertyValueIncrease
            )
        )
    }

    private fun calculateCarbonSequestration(height: Double?, diameter: Double?, age: Int?): Double {
        if (height == null || diameter == null) return 0.0
        
        // Formule simplifiée basée sur le volume de l'arbre
        val volume = PI * (diameter / 200).pow(2) * height // Volume en m³
        val carbonPerYear = volume * 0.5 // Estimation de carbone séquestré par an
        val years = age?.toDouble() ?: 10.0
        
        return carbonPerYear * years
    }

    private fun calculateOxygenProduction(height: Double?, diameter: Double?): Double {
        if (height == null || diameter == null) return 0.0
        
        // Estimation basée sur la surface foliaire
        val leafArea = PI * (diameter / 2).pow(2) // Surface foliaire en m²
        return leafArea * 0.5 // kg d'oxygène par jour
    }

    private fun calculateStormwaterReduction(diameter: Double?): Double {
        if (diameter == null) return 0.0
        
        // Estimation basée sur la surface de la canopée
        val canopyArea = PI * (diameter / 2).pow(2)
        return canopyArea * 0.8 // Réduction en litres par an
    }

    private fun calculateEnergySavings(height: Double?, diameter: Double?): Double {
        if (height == null || diameter == null) return 0.0
        
        // Estimation basée sur l'ombre et le vent
        val shadeArea = PI * (diameter / 2).pow(2)
        return shadeArea * 0.2 // Économies en kWh par an
    }

    private fun calculatePropertyValueIncrease(species: String, height: Double?, diameter: Double?): Double {
        if (height == null || diameter == null) return 0.0
        
        // Estimation basée sur la taille et l'espèce
        val baseValue = when {
            height > 10.0 -> 5000.0
            height > 5.0 -> 3000.0
            else -> 1000.0
        }
        
        val speciesMultiplier = when (species.lowercase()) {
            "chêne" -> 1.5
            "érable" -> 1.3
            "tilleul" -> 1.2
            else -> 1.0
        }
        
        return baseValue * speciesMultiplier
    }

    private fun generateImpactRecommendations(
        carbonSequestration: Double,
        oxygenProduction: Double,
        stormwaterReduction: Double,
        energySavings: Double,
        propertyValueIncrease: Double
    ): List<String> {
        val recommendations = mutableListOf<String>()

        when {
            carbonSequestration > 1000 -> {
                recommendations.add("🌳 Cet arbre est un champion de la séquestration du carbone !")
                recommendations.add("💚 Il compense les émissions annuelles de ${(carbonSequestration/1000).toInt()} voitures.")
            }
            carbonSequestration > 500 -> {
                recommendations.add("🌱 Bonne séquestration de carbone. Continuez à en prendre soin !")
            }
        }

        when {
            oxygenProduction > 50 -> {
                recommendations.add("🌬️ Production d'oxygène exceptionnelle : suffisante pour ${(oxygenProduction/2).toInt()} personnes.")
            }
        }

        when {
            stormwaterReduction > 10000 -> {
                recommendations.add("💧 Réduction significative des eaux pluviales : ${(stormwaterReduction/1000).toInt()} m³ par an.")
            }
        }

        when {
            energySavings > 100 -> {
                recommendations.add("⚡ Économies d'énergie importantes : ${energySavings.toInt()} kWh par an.")
            }
        }

        when {
            propertyValueIncrease > 5000 -> {
                recommendations.add("💰 Augmentation de la valeur immobilière estimée à ${propertyValueIncrease.toInt()}€.")
            }
        }

        return recommendations
    }
}

data class TreeImpact(
    val carbonSequestration: Double, // kg de CO2
    val oxygenProduction: Double, // kg par jour
    val stormwaterReduction: Double, // litres par an
    val energySavings: Double, // kWh par an
    val propertyValueIncrease: Double, // euros
    val recommendations: List<String>
) 