package com.example.sortinggame

import android.os.Bundle
import android.view.DragEvent
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() { 

    private val labelsPartA = mutableListOf<TextView>()
    private val labelsPartB = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val partA = findViewById<LinearLayout>(R.id.partA)
        val partB = findViewById<LinearLayout>(R.id.partB)
        val verifyButton = findViewById<Button>(R.id.verifyButton)

        fun createLabel(number: Int, parent: LinearLayout): CardView {
            val cardView = CardView(this@MainActivity)
            val cardParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val padding = resources.getDimensionPixelSize(R.dimen.card_padding)
            cardParams.setMargins(padding, padding, padding, padding)
            cardView.layoutParams = cardParams
            cardView.cardElevation = resources.getDimension(R.dimen.card_elevation)
            cardView.radius = resources.getDimension(R.dimen.card_corner_radius)

            val label = TextView(this@MainActivity)
            label.text = number.toString()
            label.textSize = 18f
            label.setPadding(padding, padding, padding, padding)

            cardView.addView(label)

            val gestureDetector = GestureDetector(this@MainActivity, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    // Double-tap logic to move cards between partA and partB
                    parent.removeView(cardView)
                    if (parent == partA) {
                        partB.addView(cardView)
                        labelsPartA.remove(label)
                        labelsPartB.add(label)
                    }
                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    // Single-tap logic to move cards back to partA
                    val cardIndex = labelsPartB.indexOfFirst { it.text == (label.text) }
                    if (cardIndex != -1) {
                        val removedLabel = labelsPartB.removeAt(cardIndex)
                        labelsPartA.add(removedLabel)

                        val parentView = cardView.parent as? ViewGroup
                        parentView?.removeView(cardView)
                        partA.addView(cardView)
                    }
                    return true
                }
            })

            cardView.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                true
            }

            return cardView
        }

        fun verifyOrder(): Boolean {
            val sortedLabels = labelsPartB.map { it.text.toString().toInt() }.sorted()
            val originalLabels = labelsPartB.map { it.text.toString().toInt() }
            return sortedLabels == originalLabels
        }

        val numbersPartA = listOf(5, 2, 8, 1)
        numbersPartA.forEach { number ->
            val card = createLabel(number, partA)
            labelsPartA.add(card.getChildAt(0) as TextView)
            partA.addView(card)
        }

        // Set up drag-and-drop listeners for partA and partB
        partA.setOnDragListener(DragListener(partA, partB, partA))
        partB.setOnDragListener(DragListener(partB, partB, partA))

        verifyButton.setOnClickListener {
            val isSuccess = verifyOrder()
            val message = if (isSuccess) "Success! The numbers are in order." else "Incorrect order."
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Drag-and-drop listener implementation
    private inner class DragListener(
        private val targetLayout: LinearLayout,
        private val partB: LinearLayout,
        private val sourceLayout: LinearLayout
    ) : View.OnDragListener {

        override fun onDrag(v: View?, event: DragEvent?): Boolean {
            when (event?.action) {
                DragEvent.ACTION_DROP -> {
                    val draggedView = event.localState as CardView
                    val parent = draggedView.parent as LinearLayout

                    parent.removeView(draggedView)
                    targetLayout.addView(draggedView)

                    // Update the lists based on the target and source layouts
                    val label = draggedView.getChildAt(0) as? TextView
                    label?.let {
                        if (targetLayout == partB) {
                            labelsPartA.remove(it)
                            labelsPartB.add(it)
                        } else {
                            labelsPartB.remove(it)
                            labelsPartA.add(it)
                        }
                    }

                    return true
                }
            }
            return true
        }
    }
}
