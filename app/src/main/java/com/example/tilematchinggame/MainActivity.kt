package com.example.tilematchinggame

import android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var startButton: Button
    private lateinit var scoreTextView: TextView
    private lateinit var titleTextView: TextView

    private var tileNumbers = mutableListOf<Int>()
    private var buttons = mutableListOf<Button>()
    private var selectedIndices = mutableListOf<Int>()
    private var matchedPairs = 0
    private var startTime: Long = 0

    private lateinit var soundPool: SoundPool
    private var selectSoundId: Int = 0
    private var matchSoundId: Int = 0
    private var wrongSoundId: Int = 0
    private var fanfareSoundId: Int = 0
    private var instructionSoundId: Int = 0 // 説明音声

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gridLayout = findViewById(R.id.gridLayout)
        startButton = findViewById(R.id.startButton)
        scoreTextView = findViewById(R.id.scoreTextView)
        titleTextView = findViewById(R.id.titleTextView)

        titleTextView.textSize = 40f
        scoreTextView.textSize = 40f
        startButton.textSize = 40f

        // 効果音をロード
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        selectSoundId = soundPool.load(this, R.raw.select_sound, 1)
        matchSoundId = soundPool.load(this, R.raw.match_sound, 1)
        wrongSoundId = soundPool.load(this, R.raw.wrong_sound, 1)
        fanfareSoundId = soundPool.load(this, R.raw.fanfare_sound, 1)
        instructionSoundId = soundPool.load(this, R.raw.instruction, 1) // 説明音声をロード

        startButton.setOnClickListener {
            playInstruction()
            startGame()
        }
    }

    private fun playInstruction() {
        // 説明音声を再生
        soundPool.play(instructionSoundId, 1f, 1f, 0, 0, 1f)
    }

    private fun startGame() {
        gridLayout.removeAllViews()
        buttons.clear()
        selectedIndices.clear()
        matchedPairs = 0
        startTime = SystemClock.elapsedRealtime()

        scoreTextView.text = "タイム : ーー"
        scoreTextView.setTextColor(Color.BLACK)
        startButton.text = "ゲーム中"

        tileNumbers = generateShuffledPairs(4)

        tileNumbers.forEachIndexed { index, number ->
            val button = Button(this).apply {
                text = "?"
                textSize = 100f
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                setOnClickListener { onTileClick(index) }
            }
            buttons.add(button)
            gridLayout.addView(button)
        }
    }

    private fun onTileClick(index: Int) {
        if (selectedIndices.size < 2 && buttons[index].text == "?") {
            // 再生: カード選択音
            soundPool.play(selectSoundId, 1f, 1f, 0, 0, 1f)

            buttons[index].text = tileNumbers[index].toString()
            selectedIndices.add(index)

            if (selectedIndices.size == 2) {
                Handler(Looper.getMainLooper()).postDelayed({
                    checkMatch()
                }, 1000)
            }
        }
    }

    private fun checkMatch() {
        val firstIndex = selectedIndices[0]
        val secondIndex = selectedIndices[1]

        if (tileNumbers[firstIndex] == tileNumbers[secondIndex]) {
            // 再生: 一致音
            soundPool.play(matchSoundId, 1f, 1f, 0, 0, 1f)

            buttons[firstIndex].visibility = Button.INVISIBLE
            buttons[secondIndex].visibility = Button.INVISIBLE
            matchedPairs++
        } else {
            // 再生: 不一致音
            soundPool.play(wrongSoundId, 1f, 1f, 0, 0, 1f)

            buttons[firstIndex].text = "?"
            buttons[secondIndex].text = "?"
        }
        selectedIndices.clear()

        if (matchedPairs == 4) {
            val elapsedTime = (SystemClock.elapsedRealtime() - startTime) / 1000.0
            scoreTextView.text = String.format("おめでとうございます！ タイムは : %.2f 秒", elapsedTime)
            scoreTextView.setTextColor(Color.RED)

            // 再生: ファンファーレ
            soundPool.play(fanfareSoundId, 1f, 1f, 0, 0, 1f)

            startButton.text = "スタート"
        }
    }

    private fun generateShuffledPairs(size: Int): MutableList<Int> {
        return (1..size).flatMap { listOf(it, it) }.shuffled().toMutableList()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}
