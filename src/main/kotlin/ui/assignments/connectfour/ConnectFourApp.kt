package ui.assignments.connectfour

import javafx.application.Application
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import javafx.stage.Stage
import javafx.animation.Transition
import javafx.animation.TranslateTransition
import javafx.scene.text.FontPosture
import ui.assignments.connectfour.model.Model
import java.time.Duration
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.beans.value.ChangeListener
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Shape
import ui.assignments.connectfour.model.Player
import kotlin.properties.Delegates

interface IView {
    fun updateView()
}

class Exit(val stage: Stage) : Button("Exit") {
   init{
       prefWidth = 80.0
       prefHeight= 50.0
       padding = Insets( 20.0)
       alignment= Pos.CENTER
       background = Background(
           BackgroundFill(
               Color.LIGHTGREEN,
               CornerRadii(5.0),
               Insets(0.0)))
       font = Font.font("Times New Roman", FontPosture.REGULAR, 20.0)

       onAction = EventHandler {
            stage.close()
       }
   }
}
class Submit(text : TextArea) : Button("Submit") {
    init {
        prefWidth = 120.0
        onAction = EventHandler {
            text.clear()
        }
    }
}

class Controller(val model : GameModel) : Button("Click here to start game!"){
   // var isGameDone = false
    init {
        prefWidth = 480.0
        prefHeight = 80.0
        textAlignment = TextAlignment.CENTER
        background = Background(
           BackgroundFill(
               Color.LIGHTSEAGREEN,
               CornerRadii(5.0),
               Insets(0.0)))

       font = Font.font("Times New Roman", FontPosture.REGULAR, 20.0)
        onAction = EventHandler {
            Model.onNextPlayer.addListener{
                    _,_, new ->
                if( new == Player.ONE){
                    model.displayPlayerPiece(model.makeNewPiece("1"), new.toString())
                } else if ( new  == Player.TWO){
                    model.displayPlayerPiece(model.makeNewPiece("2"), new.toString())
                }

            }
            Model.startGame()
            isDisabled = true;
            isVisible = false;

        }
    }
}
data class DragInfo(var target : ImageView? = null,
                    var anchorX: Double = 0.0,
                    var anchorY: Double = 0.0,
                    var initialX: Double = 0.0,
                    var initialY: Double = 0.0)
class GameModel(stage: Stage){
    private val views: ArrayList<IView> = ArrayList()
    var currentCol = 0
    val player1Lb = Label("Player #1").apply {
        font = Font.font("Times New Roman", FontPosture.REGULAR, 20.0)
    }
    val player1HB = HBox(player1Lb).apply {
        padding =  Insets(10.0)
    }
    val player2Lb = Label("Player #2").apply {
        font = Font.font("Times New Roman", FontPosture.REGULAR, 20.0)
    }
    val player2HB = HBox(player2Lb).apply {
        padding =  Insets(10.0)
    }
    val startBtn = Controller(this)
    val exitBtn = Exit(stage)
    val feedbackText = TextArea("Please enter your feedback").apply{
        prefWidth = 120.0
        prefHeight = 50.0
        isWrapText = true
    }
    val feedBackBox = VBox(feedbackText,Submit(feedbackText))
    val rightBox = Pane(player2HB)
    val leftBox = Pane(player1HB)
    var gridNode = ImageView(
        javaClass.getResource("/ui/assignments/connectfour/grid_8x7.png")?.toString()
            ?: throw IllegalArgumentException("Image file not found")
    )
    val centreBox = VBox(startBtn, gridNode).apply {
        spacing = 10.0
        padding = Insets(40.0, 0.0, 0.0, 0.0)
    }

    fun addView(view: IView) {
        views.add(view)
        view.updateView()
    }
    fun gameOverImage(playerNum: String){
        var player1won = ImageView(javaClass.getResource("/ui/assignments/connectfour/player1_win.png")?.toString() ?:
        throw IllegalArgumentException("Image file not found"))
        var player2won = ImageView(javaClass.getResource("/ui/assignments/connectfour/player2_win.png")?.toString() ?:
        throw IllegalArgumentException("Image file not found"))
        var draw = ImageView(javaClass.getResource("/ui/assignments/connectfour/draw.png")?.toString() ?:
        throw IllegalArgumentException("Image file not found"))
        player1won.fitWidth = 650.0
        player1won.fitHeight = 500.0
        player2won.fitWidth = 650.0
        player2won.fitHeight = 500.0
        draw.fitWidth = 650.0
        draw.fitHeight = 500.0
        centreBox.children.remove(0,2)
        leftBox.getChildren().clear()
        rightBox.children.clear()
        leftBox.children.add(exitBtn)
        rightBox.children.add(feedBackBox)
        if(playerNum == "1"){
            centreBox.children.add(player1won)
        }else if (playerNum == "2"){
            centreBox.children.add(player2won)
        } else {
            centreBox.children.add(draw)
        }
    }
    fun makeNewPiece (playerColour: String) : ImageView{
        var redPlayerPc = ImageView(javaClass.getResource("/ui/assignments/connectfour/piece_red.png")?.toString() ?:
        throw IllegalArgumentException("Image file not found"))
        var yellowPlayerPc = ImageView(javaClass.getResource("/ui/assignments/connectfour/piece_yellow.png")?.toString() ?:
        throw IllegalArgumentException("Image file not found"))

        if(playerColour == "1"){
            return redPlayerPc
        } else {
            return yellowPlayerPc
        }
    }

    fun findColumn (xMouse : Double) : Int {
        var currentCol = (xMouse / 80).toInt()
        return currentCol
    }
    fun displayPlayerPiece(playerCol : ImageView, playerString : String) {
        var dragInfo = DragInfo()
        playerCol.apply {
            fitWidth = 65.0
            fitHeight = 65.0

            addEventFilter(MouseEvent.MOUSE_PRESSED) {
                dragInfo = DragInfo(this, it.sceneX, it.sceneY,
                    translateX, translateY)
            }
            addEventFilter(MouseEvent.MOUSE_DRAGGED) {
                translateX = dragInfo.initialX + it.sceneX - dragInfo.anchorX
                translateY = dragInfo.initialY + it.sceneY - dragInfo.anchorY

                if(playerString == "1") {
                    // Force player piece to centre above at the closest slot
                    var currentCol  = findColumn (it.sceneX)
                    if(it.sceneX >= 120 && it.sceneX <=760) {
                        when (currentCol) {
                            1 -> {
                                translateX = 120.0
                            }
                            2 -> {
                                translateX = 200.0
                            }
                            3 -> {
                                translateX = 280.0
                            }
                            4 -> {
                                translateX = 360.0
                            }
                            5 -> {
                                translateX = 440.0
                            }
                            6 -> {
                                translateX = 520.0
                            }
                            7 -> {
                                translateX = 600.0
                            }

                            8 -> {
                                translateX = 680.0
                            }
                        }
                    }

                    // Constrain player piece from leaving application window
                    if(it.sceneY < 35.0){
                        translateY = 0.0
                    }
                    if((it.sceneX < 35.0)){
                        translateX = 0.0
                    }
                    if(it.sceneY > 650.0){
                        translateY = 585.0
                    }
                    if(it.sceneX > 850){
                        translateX = 810.0
                    }

                    // Constrain player piece from interfering with grid
                    if((it.sceneX > 50 && it.sceneX < 130) && it.sceneY > 120){
                        translateX = 40.0
                    } else if ((it.sceneX > 720 && it.sceneX < 900) && it.sceneY > 120){
                        translateX =  755.0
                    } else if ((it.sceneX >= 130 && it.sceneX <= 770) &&  it.sceneY > 100){
                        translateY =  17.0
                    }
                } else if (playerString == "2"){
                    // Force player piece to centre above at the closest slot
                    var currentCol  = findColumn (it.sceneX)
                    if(it.sceneX >= 120 && it.sceneX <=730){
                        when (currentCol) {
                            1 -> {
                                translateX = -650.0
                            }
                            2 -> {
                                translateX = -570.0
                            }
                            3 -> {
                                translateX = -490.0
                            }
                            4 -> {
                                translateX = -410.0
                            }
                            5 -> {
                                translateX = -330.0
                            }
                            6 -> {
                                translateX = -250.0
                            }
                            7 -> {
                                translateX = -170.0
                            }
                            8 -> {
                                translateX = -90.0
                            }
                        }
                    }
                    // Constrain player piece from leaving application window
                    if(it.sceneY < 35.0){
                        translateY = 0.0
                    }
                    if((it.sceneX < 35.0)){
                        translateX = -780.0
                    }
                    if(it.sceneY > 650.0){
                        translateY = 585.0 //585
                    }
                    if(it.sceneX > 860){
                        translateX =  40.0  //810
                    }

                    // Constrain player piece from interfering with grid
                    if((it.sceneX > 50 && it.sceneX < 130) && it.sceneY > 120){
                        translateX = -730.0
                    } else if ((it.sceneX > 720 && it.sceneX < 900) && it.sceneY > 120){
                        translateX = -15.0
                    } else if ((it.sceneX >= 130 && it.sceneX <= 770) &&  it.sceneY > 100){
                        translateY =  17.0
                    }


                }
            }
            addEventFilter(MouseEvent.MOUSE_RELEASED) {
                dragInfo = DragInfo()
                currentCol = findColumn (it.sceneX) - 1
                if( (currentCol < 8 && currentCol >= 0) && (it.sceneX > 120 && it.sceneY < 670)) {
                    Model.dropPiece(currentCol)
                }
                var animEndValue = 0.0
                if(Model.onPieceDropped.value != null) {
                    animEndValue = (65.0 * (Model.onPieceDropped.value?.y!! + 1)) + 120.0
                    if ((Model.onPieceDropped.value?.y!! + 1) < 7) {

                        animEndValue -= 20.0
                        if ((Model.onPieceDropped.value?.y!! + 1) < 6) {
                            animEndValue -= 10.0
                        }
                        if ((Model.onPieceDropped.value?.y!! + 1) < 5) {
                            animEndValue -= 15.0
                        }
                        if ((Model.onPieceDropped.value?.y!! + 1) < 4) {
                            animEndValue -= 15.0
                        }
                        if ((Model.onPieceDropped.value?.y!! + 1) < 3) {
                            animEndValue -= 15.0
                        }
                        if ((Model.onPieceDropped.value?.y!! + 1) < 2) {
                            animEndValue -= 15.0
                        }
                    }
                    val anim = TranslateTransition(javafx.util.Duration(300.0), this).apply {
                        toY = animEndValue
                        interpolator = Interpolator.EASE_BOTH
                    }
                    anim.play()
                    anim.setOnFinished {
                        // Controls when game is over based on a win by listening to onGameWin and displaying the appropriate image
                        if(Model.onGameWin.value == Player.ONE || Model.onGameWin.value == Player.TWO){
                            if(Model.onGameWin.value == Player.ONE){
                                gameOverImage("1")
                            } else if (Model.onGameWin.value == Player.TWO){
                                gameOverImage("2")
                            }
                        }

                        // Controls when game is over based on a draw by listening to onGameDraw and displaying an image
                        if(Model.onGameDraw.value == true){
                            gameOverImage("3")
                        }
                    }
                    isDisable = true
                } else {
                    var endvalueX = 0.0
                    var endValueY = 0.0
                    val animFail = TranslateTransition(javafx.util.Duration(300.0), this).apply {
                        toY = endValueY
                        toX = endvalueX
                        interpolator = Interpolator.EASE_BOTH
                    }
                    animFail.play()
                }
            }

        }

        playerCol.x = 20.0
        playerCol.y = 50.0
        if(playerString == "1"){
            leftBox.children.add(playerCol)
        } else if(playerString == "2"){
            rightBox.children.add(playerCol)
        }
    }
    init {
        gridNode.fitWidth = 640.0
        gridNode.fitHeight = 560.0

        rightBox.padding = Insets(10.0)
        rightBox.prefWidth = 130.0
        leftBox.padding = Insets(10.0)
        leftBox.prefWidth = 130.0
        centreBox.setAlignment(Pos.CENTER)
    }
}

class ConnectFourApp : Application() {
    override fun start(stage: Stage) {

        val model = GameModel(stage)
        val root = BorderPane()
        root.center = model.centreBox
        root.right = model.rightBox
        root.left = model.leftBox
        val scene = Scene(root, 900.0, 700.0)
        stage.title = "CS349 - A3 Connect Four - slev"
        stage.scene = scene
        stage.isResizable = false
        stage.show()
    }
}