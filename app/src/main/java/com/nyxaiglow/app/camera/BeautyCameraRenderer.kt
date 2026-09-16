package com.nyxaiglow.app.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.view.Surface
import androidx.camera.core.SurfaceRequest
import com.nyxaiglow.app.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.Executor
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class BeautyCameraRenderer(
    context: Context,
    private val callbackExecutor: Executor,
    private val onSurfaceError: (String) -> Unit = {}
) : GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private val fragmentShaderSource = context.resources.openRawResource(R.raw.beauty_shader)
        .bufferedReader()
        .use { it.readText() }
    private val vertexBuffer = createFloatBuffer(
        floatArrayOf(
            -1f, -1f, 0f, 1f,
             1f, -1f, 1f, 1f,
            -1f,  1f, 0f, 0f,
             1f,  1f, 1f, 0f
        )
    )
    private val textureMatrix = FloatArray(16)
    private val lock = Any()
    private var surfaceTexture: SurfaceTexture? = null
    private var cameraSurface: Surface? = null
    private var pendingRequest: SurfaceRequest? = null
    private var textureId = 0
    private var makeupTextureId = 0
    private var program = 0
    private var glowStrength = 0.68f
    private var smoothStrength = 0.2f
    private var released = false
    private var updateTexture = false
    @Volatile
    private var requestRender: (() -> Unit)? = null
    private var positionLocation = -1
    private var textureCoordLocation = -1
    private var textureMatrixLocation = -1
    private var glowLocation = -1
    private var smoothLocation = -1
    private var makeupMaskLocation = -1

    fun setGlowStrength(value: Float) {
        glowStrength = value.coerceIn(0f, 1f)
    }

    fun setSmoothStrength(value: Float) {
        smoothStrength = value.coerceIn(0f, 1f)
    }

    fun updateMakeupMask(bitmap: Bitmap) {
        if (released) {
            bitmap.recycle()
            return
        }
        if (makeupTextureId == 0) {
            val textures = IntArray(1)
            GLES20.glGenTextures(1, textures, 0)
            makeupTextureId = textures[0]
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, makeupTextureId)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, makeupTextureId)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
    }

    fun setRenderRequest(callback: (() -> Unit)?) {
        requestRender = callback
    }

    fun provideSurfaceRequest(request: SurfaceRequest) {
        synchronized(lock) {
            if (released) {
                request.willNotProvideSurface()
                return
            }
            pendingRequest?.willNotProvideSurface()
            pendingRequest = request
            providePendingRequestLocked()
        }
    }

    fun release(onComplete: () -> Unit = {}) {
        synchronized(lock) {
            released = true
            pendingRequest?.willNotProvideSurface()
            pendingRequest = null
            cameraSurface?.release()
            cameraSurface = null
            surfaceTexture?.release()
            surfaceTexture = null
            if (makeupTextureId != 0) {
                GLES20.glDeleteTextures(1, intArrayOf(makeupTextureId), 0)
                makeupTextureId = 0
            }
            if (textureId != 0) {
                GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
                textureId = 0
            }
            if (program != 0) {
                GLES20.glDeleteProgram(program)
                program = 0
            }
        }
        callbackExecutor.execute(onComplete)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        try {
            GLES20.glClearColor(0f, 0f, 0f, 1f)
            textureId = createExternalTexture()
            surfaceTexture = SurfaceTexture(textureId).also {
                it.setOnFrameAvailableListener(this)
            }
            cameraSurface = Surface(surfaceTexture)
            val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource)
            val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource)
            program = GLES20.glCreateProgram()
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)
            checkGlLink(program)
            GLES20.glDeleteShader(vertexShader)
            GLES20.glDeleteShader(fragmentShader)
            positionLocation = GLES20.glGetAttribLocation(program, "aPosition")
            textureCoordLocation = GLES20.glGetAttribLocation(program, "aTextureCoord")
            textureMatrixLocation = GLES20.glGetUniformLocation(program, "uTextureMatrix")
            glowLocation = GLES20.glGetUniformLocation(program, "uGlowStrength")
            smoothLocation = GLES20.glGetUniformLocation(program, "uSmoothStrength")
            makeupMaskLocation = GLES20.glGetUniformLocation(program, "uMakeupMask")
            updateMakeupMask(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
            synchronized(lock) { providePendingRequestLocked() }
        } catch (exception: Exception) {
            callbackExecutor.execute { onSurfaceError("Renderer initialization failed: ${exception.message}") }
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        val texture = surfaceTexture ?: return
        synchronized(this) {
            if (updateTexture) {
                texture.updateTexImage()
                texture.getTransformMatrix(textureMatrix)
                updateTexture = false
            }
        }
        if (program == 0) return
        GLES20.glUseProgram(program)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "uTexture"), 0)
        GLES20.glUniform1f(glowLocation, glowStrength)
        GLES20.glUniform1f(smoothLocation, smoothStrength)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, makeupTextureId)
        GLES20.glUniform1i(makeupMaskLocation, 1)
        GLES20.glUniformMatrix4fv(textureMatrixLocation, 1, false, textureMatrix, 0)
        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(positionLocation)
        GLES20.glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)
        vertexBuffer.position(2)
        GLES20.glEnableVertexAttribArray(textureCoordLocation)
        GLES20.glVertexAttribPointer(textureCoordLocation, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(positionLocation)
        GLES20.glDisableVertexAttribArray(textureCoordLocation)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        synchronized(this) { updateTexture = true }
        requestRender?.invoke()
    }

    private fun providePendingRequestLocked() {
        val request = pendingRequest ?: return
        val surface = cameraSurface ?: return
        pendingRequest = null
        request.provideSurface(surface, callbackExecutor) { result ->
            if (result.resultCode != SurfaceRequest.Result.RESULT_SURFACE_USED_SUCCESSFULLY) {
                callbackExecutor.execute {
                    onSurfaceError("Camera surface ended: ${result.resultCode}")
                }
            }
        }
    }

    private fun createExternalTexture(): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        return textures[0]
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            error("Shader compilation failed: $log")
        }
        return shader
    }

    private fun checkGlLink(program: Int) {
        val status = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0)
        check(status[0] != 0) { "Program link failed: ${GLES20.glGetProgramInfoLog(program)}" }
    }

    private fun createFloatBuffer(data: FloatArray): FloatBuffer = ByteBuffer
        .allocateDirect(data.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(data)
            position(0)
        }

    private companion object {
        const val vertexShaderSource = """
            attribute vec4 aPosition;
            attribute vec2 aTextureCoord;
            uniform mat4 uTextureMatrix;
            varying vec2 vTextureCoord;
            void main() {
                gl_Position = aPosition;
                vTextureCoord = (uTextureMatrix * vec4(aTextureCoord, 0.0, 1.0)).xy;
            }
        """
    }
}
