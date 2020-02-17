import java.util.function.Supplier

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import akka.stream.{Attributes, Outlet, SourceShape}
import javax.swing.WindowConstants
import org.bytedeco.javacv.{CanvasFrame, Frame, OpenCVFrameConverter}
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_videoio.VideoCapture
import org.bytedeco.opencv.global.opencv_core._

class WebcamFrameSource extends GraphStage[SourceShape[Mat]] {
  val out: Outlet[Mat] = Outlet("WebcamFrameSource")
  override val shape: SourceShape[Mat] = SourceShape(out)
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      val vidCap = new VideoCapture()
      var mat = new Mat()

      override def preStart(): Unit = {
        vidCap.open(0)
      }

      override def postStop(): Unit = {
        vidCap.release()
      }

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          vidCap.grab()
          vidCap.retrieve(mat)

          push(out, mat)
        }
      })

    }

}

object Main extends App{
  implicit val system = ActorSystem("webcam")

  val canvas = new CanvasFrame("Webcam", 1)
  canvas.setCanvasSize(640, 480)
  canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

  val sourceGraph = new WebcamFrameSource
  val webcamSource = Source.fromGraph(sourceGraph)


  webcamSource // most OpenCV manipulations require a Matrix
    .map(Flip.horizontal)
    .map(MediaConversion.toFrame) // convert back to a frame
    .map(canvas.showImage)
    .to(Sink.ignore)
    .run()
}

object MediaConversion {

  // Each thread gets its own greyMat for safety
  private val frameToMatConverter = ThreadLocal.withInitial(new Supplier[OpenCVFrameConverter.ToMat] {
    def get(): OpenCVFrameConverter.ToMat = new OpenCVFrameConverter.ToMat
  })

  /**
   * Returns an OpenCV Mat for a given JavaCV frame
   */
//  def toMat(frame: Frame): Mat = frameToMatConverter.get().convert(frame)

  /**
   * Returns a JavaCV Frame for a given OpenCV Mat
   */
  def toFrame(mat: Mat): Frame = frameToMatConverter.get().convert(mat)

}

object Flip {

  /**
   * Clones the image and returns a flipped version of the given image matrix along the y axis (horizontally)
   */
  def horizontal(mat: Mat): Mat = {
    val cloned = mat.clone()
    flip(cloned, cloned, 1)
    cloned
  }

}





