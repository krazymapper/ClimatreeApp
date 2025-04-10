package com.example.touuri.data

import android.content.Context
import android.net.Uri
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture

class TreeARService(private val context: Context) {
    private var treeModel: ModelRenderable? = null
    private var impactNode: ViewRenderable? = null
    private var futureTreeModel: CompletableFuture<ModelRenderable>? = null
    private var futureImpactNode: CompletableFuture<ViewRenderable>? = null

    suspend fun loadModels(species: String) = withContext(Dispatchers.IO) {
        // Charger le modèle 3D de l'arbre selon l'espèce
        val modelUri = when (species.lowercase()) {
            "chêne" -> "models/oak_tree.sfb"
            "érable" -> "models/maple_tree.sfb"
            "tilleul" -> "models/linden_tree.sfb"
            else -> "models/generic_tree.sfb"
        }

        futureTreeModel = ModelRenderable.builder()
            .setSource(context, Uri.parse(modelUri))
            .build()

        // Charger le nœud d'affichage des impacts
        futureImpactNode = ViewRenderable.builder()
            .setView(context, R.layout.ar_impact_view)
            .build()

        // Attendre le chargement des modèles
        treeModel = futureTreeModel?.get()
        impactNode = futureImpactNode?.get()
    }

    fun createTreeNode(
        scene: Scene,
        anchor: Anchor,
        height: Double?,
        diameter: Double?,
        impact: TreeImpact
    ): AnchorNode {
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(scene)

        // Créer le nœud de l'arbre
        val treeNode = Node().apply {
            setParent(anchorNode)
            localScale = Vector3(
                (diameter ?: 1.0).toFloat() / 100,
                (height ?: 5.0).toFloat(),
                (diameter ?: 1.0).toFloat() / 100
            )
            renderable = treeModel
        }

        // Créer le nœud d'impact
        val impactNode = Node().apply {
            setParent(anchorNode)
            localPosition = Vector3(0f, (height ?: 5.0).toFloat() + 1f, 0f)
            renderable = this@TreeARService.impactNode
        }

        return anchorNode
    }

    fun createFutureTreeNode(
        scene: Scene,
        anchor: Anchor,
        currentHeight: Double?,
        currentDiameter: Double?,
        futureHeight: Double?,
        futureDiameter: Double?,
        impact: TreeImpact
    ): AnchorNode {
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(scene)

        // Créer le nœud de l'arbre actuel (semi-transparent)
        val currentTreeNode = Node().apply {
            setParent(anchorNode)
            localScale = Vector3(
                (currentDiameter ?: 1.0).toFloat() / 100,
                (currentHeight ?: 5.0).toFloat(),
                (currentDiameter ?: 1.0).toFloat() / 100
            )
            renderable = treeModel?.let { model ->
                model.makeCopy().apply {
                    material?.setFloat("baseColorFactor", 0.5f)
                }
            }
        }

        // Créer le nœud de l'arbre futur
        val futureTreeNode = Node().apply {
            setParent(anchorNode)
            localScale = Vector3(
                (futureDiameter ?: 1.0).toFloat() / 100,
                (futureHeight ?: 5.0).toFloat(),
                (futureDiameter ?: 1.0).toFloat() / 100
            )
            renderable = treeModel
        }

        // Créer le nœud d'impact futur
        val impactNode = Node().apply {
            setParent(anchorNode)
            localPosition = Vector3(0f, (futureHeight ?: 5.0).toFloat() + 1f, 0f)
            renderable = this@TreeARService.impactNode
        }

        return anchorNode
    }

    fun cleanup() {
        futureTreeModel?.cancel(true)
        futureImpactNode?.cancel(true)
        treeModel = null
        impactNode = null
    }
} 