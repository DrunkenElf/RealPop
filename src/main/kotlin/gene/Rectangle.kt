import java.awt.Color
import java.awt.Graphics

interface Shape {
    var color: IntArray // RGBA
    var isOld: Boolean
    var isMutated: Boolean
    var error: Double
    var canChange: Boolean
    fun draw(g: Graphics, context: Context)
    fun copy(): Shape
}

class Rectangle(
    override var color: IntArray,
    var x: Int,
    var y: Int,
    var w: Int,
    var h: Int,
    override var canChange: Boolean = true,
    override var isOld: Boolean = false,
    override var isMutated: Boolean = false,
    override var error: Double = Double.MAX_VALUE,
) : Shape {

    override fun draw(g: Graphics, context: Context) {

        if (context.useAlpha) {
            g.color = Color(color.get(0), color.get(1), color.get(2), color.get(3))
        } else {
            g.color = Color(color.get(0), color.get(1), color.get(2))
        }

        g.fillRect(x, y, w, h)
    }

    override fun copy(): Rectangle {
        return Rectangle(
            color = color,
            x = x,
            y = y,
            w = w,
            h = h
        )
    }

}