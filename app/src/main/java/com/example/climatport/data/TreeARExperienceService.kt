package com.example.climatport.data

import android.content.Context
import android.net.Uri
import com.google.ar.core.*
import com.google.android.filament.*
import com.google.android.filament.gltfio.*
import com.google.android.filament.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableException
import java.util.concurrent.atomic.AtomicReference

class TreeARExperienceService(private val context: Context) {
    private var engine: Engine? = null
    private var scene: Scene? = null
    private var view: View? = null
    private var renderer: Renderer? = null
    private var assetLoader: AssetLoader? = null
    private var treeInstance: FilamentAsset? = null
    private var impactInstance: FilamentAsset? = null
    private val arSession = AtomicReference<Session?>(null)

    data class TreeARModel(
        val species: String,
        val height: Float,
        val diameter: Float,
        val age: Int,
        val environmentalImpact: EnvironmentalImpact,
        val futureProjection: FutureProjection
    )

    data class EnvironmentalImpact(
        val co2Absorbed: Double, // kg de CO2 absorbés
        val oxygenProduced: Double, // kg d'oxygène produits
        val waterRetained: Double, // litres d'eau retenus
        val temperatureReduction: Double, // degrés Celsius
        val biodiversityScore: Int // score de biodiversité
    )

    data class FutureProjection(
        val co2Projection: List<Double>, // projection sur 10 ans
        val growthProjection: List<Float>, // projection de croissance
        val healthProjection: List<Int> // projection de santé (0-100)
    )

    suspend fun initialize() = withContext(Dispatchers.IO) {
        engine = Engine.create()
        scene = Scene(engine!!)
        view = View(engine!!)
        renderer = Renderer(engine!!)
        assetLoader = AssetLoader(engine!!, MaterialProvider(engine!!), EntityManager.get())

        // Configuration de la scène
        scene?.apply {
            skybox = Skybox(engine!!)
            indirectLight = IndirectLight.Builder()
                .reflectionsCubemap(engine!!, createReflectionsCubemap())
                .build(engine!!)
        }

        // Configuration de la vue
        view?.apply {
            scene = this@TreeARExperienceService.scene
            camera = Camera(engine!!)
        }
    }

    suspend fun initializeAR(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            when (ArCoreApk.getInstance().requestInstall(context, true)) {
                ArCoreApk.InstallStatus.INSTALLED -> {
                    arSession.set(Session(context))
                    Result.success(Unit)
                }
                else -> Result.failure(Exception("ARCore n'est pas installé"))
            }
        } catch (e: UnavailableException) {
            Result.failure(e)
        }
    }

    fun createTreeExperience(
        species: String,
        height: Double?,
        diameter: Double?,
        impact: TreeImpact,
        showFuture: Boolean = false,
        futureHeight: Double? = null,
        futureDiameter: Double? = null
    ) {
        // Charger le modèle 3D de l'arbre
        val modelUri = when (species.lowercase()) {
            "chêne" -> "models/oak_tree.glb"
            "érable" -> "models/maple_tree.glb"
            "tilleul" -> "models/linden_tree.glb"
            else -> "models/generic_tree.glb"
        }

        // Créer l'instance de l'arbre
        treeInstance = assetLoader?.createAssetFromBinary(
            context.assets.open(modelUri).readBytes()
        )

        // Configurer l'arbre
        treeInstance?.let { instance ->
            // Ajuster la taille
            val scale = Vector3(
                (diameter ?: 1.0).toFloat() / 100,
                (height ?: 5.0).toFloat(),
                (diameter ?: 1.0).toFloat() / 100
            )
            instance.root.transformManager.setTransform(
                instance.root.entity,
                Matrix4.scale(scale)
            )

            // Ajouter des effets visuels basés sur l'impact
            addImpactEffects(instance, impact)

            // Si on montre le futur, créer une version semi-transparente
            if (showFuture) {
                createFutureVersion(
                    instance,
                    futureHeight ?: height ?: 5.0,
                    futureDiameter ?: diameter ?: 1.0
                )
            }

            scene?.addEntity(instance.root.entity)
        }
    }

    private fun addImpactEffects(instance: FilamentAsset, impact: TreeImpact) {
        // Effet de séquestration de carbone
        if (impact.carbonSequestration > 1000) {
            addParticleEffect(instance, "carbon_effect", Color(0.0f, 0.5f, 0.0f, 0.3f))
        }

        // Effet de production d'oxygène
        if (impact.oxygenProduction > 50) {
            addParticleEffect(instance, "oxygen_effect", Color(0.0f, 0.0f, 1.0f, 0.3f))
        }

        // Effet de valeur économique
        if (impact.propertyValueIncrease > 5000) {
            addParticleEffect(instance, "value_effect", Color(1.0f, 0.84f, 0.0f, 0.3f))
        }
    }

    private fun createFutureVersion(
        currentInstance: FilamentAsset,
        futureHeight: Double,
        futureDiameter: Double
    ) {
        val futureInstance = currentInstance.copy()
        val scale = Vector3(
            (futureDiameter).toFloat() / 100,
            (futureHeight).toFloat(),
            (futureDiameter).toFloat() / 100
        )
        futureInstance.root.transformManager.setTransform(
            futureInstance.root.entity,
            Matrix4.scale(scale)
        )

        // Rendre la version actuelle semi-transparente
        currentInstance.root.transformManager.setTransform(
            currentInstance.root.entity,
            Matrix4.scale(Vector3(0.5f))
        )

        scene?.addEntity(futureInstance.root.entity)
    }

    private fun addParticleEffect(
        instance: FilamentAsset,
        effectName: String,
        color: Color
    ) {
        // Créer un système de particules
        val particleSystem = ParticleSystem.Builder()
            .maxCount(1000)
            .build(engine!!)

        // Configurer les particules
        particleSystem.apply {
            setColor(color)
            setSpeed(0.1f)
            setLifetime(5.0f)
        }

        // Attacher le système de particules à l'arbre
        scene?.addEntity(particleSystem.entity)
    }

    fun render(frame: Frame) {
        renderer?.render(frame, view)
    }

    fun cleanup() {
        treeInstance?.let { scene?.removeEntity(it.root.entity) }
        impactInstance?.let { scene?.removeEntity(it.root.entity) }
        engine?.destroy()
    }

    private fun createReflectionsCubemap(): Texture {
        // Créer une texture cubemap pour les réflexions
        return Texture.Builder()
            .width(256)
            .height(256)
            .levels(1)
            .sampler(Texture.Sampler.SAMPLER_CUBEMAP)
            .format(Texture.InternalFormat.RGBA8)
            .build(engine!!)
    }

    fun createTreeARModel(
        tree: TreeData,
        impact: EnvironmentalImpact,
        projection: FutureProjection
    ): TreeARModel {
        return TreeARModel(
            species = tree.species,
            height = tree.height,
            diameter = tree.diameter,
            age = tree.age,
            environmentalImpact = impact,
            futureProjection = projection
        )
    }

    fun calculateEnvironmentalImpact(tree: TreeData): EnvironmentalImpact {
        // Calculs basés sur des formules scientifiques
        val co2Absorbed = tree.height * tree.diameter * 0.1 // kg de CO2
        val oxygenProduced = co2Absorbed * 0.7 // kg d'O2
        val waterRetained = tree.height * tree.diameter * 2.5 // litres
        val temperatureReduction = tree.height * 0.1 // degrés Celsius
        val biodiversityScore = when (tree.species) {
            "native" -> 100
            "exotic" -> 50
            else -> 75
        }
        
        return EnvironmentalImpact(
            co2Absorbed = co2Absorbed,
            oxygenProduced = oxygenProduced,
            waterRetained = waterRetained,
            temperatureReduction = temperatureReduction,
            biodiversityScore = biodiversityScore
        )
    }
    
    fun projectFutureImpact(
        currentImpact: EnvironmentalImpact,
        tree: TreeData
    ): FutureProjection {
        val years = 10
        val co2Projection = List(years) { year ->
            currentImpact.co2Absorbed * (1 + year * 0.1)
        }
        val growthProjection = List(years) { year ->
            tree.height * (1 + year * 0.05f)
        }
        val healthProjection = List(years) { year ->
            (100 - year * 2).coerceAtLeast(0)
        }
        
        return FutureProjection(
            co2Projection = co2Projection,
            growthProjection = growthProjection,
            healthProjection = healthProjection
        )
    }
    
    fun dispose() {
        arSession.get()?.close()
        arSession.set(null)
    }
} 