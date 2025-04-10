package com.example.climatree.data

import android.content.Context
import android.net.Uri
import com.google.ar.core.*
import com.google.android.filament.*
import com.google.android.filament.gltfio.*
import com.google.android.filament.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture

class TreeARExperienceService(private val context: Context) {
    private var engine: Engine? = null
    private var scene: Scene? = null
    private var view: View? = null
    private var renderer: Renderer? = null
    private var assetLoader: AssetLoader? = null
    private var treeInstance: FilamentAsset? = null
    private var impactInstance: FilamentAsset? = null

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
} 