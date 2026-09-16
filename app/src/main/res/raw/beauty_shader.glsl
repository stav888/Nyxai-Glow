#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES uTexture;
uniform sampler2D uMakeupMask;
uniform float uGlowStrength;
uniform float uSmoothStrength;
varying vec2 vTextureCoord;

void main() {
    vec4 color = texture2D(uTexture, vTextureCoord);
    vec4 mask = texture2D(uMakeupMask, vTextureCoord);
    vec2 offset = 1.0 / 512.0;
    vec3 blur = color.rgb * 4.0;
    blur += texture2D(uTexture, vTextureCoord + vec2(-offset.x, -offset.y)).rgb;
    blur += texture2D(uTexture, vTextureCoord + vec2(0.0, -offset.y)).rgb;
    blur += texture2D(uTexture, vTextureCoord + vec2(offset.x, -offset.y)).rgb;
    blur += texture2D(uTexture, vTextureCoord + vec2(-offset.x, 0.0)).rgb;
    blur += texture2D(uTexture, vTextureCoord + vec2(offset.x, 0.0)).rgb;
    blur += texture2D(uTexture, vTextureCoord + vec2(-offset.x, offset.y)).rgb;
    blur += texture2D(uTexture, vTextureCoord + vec2(0.0, offset.y)).rgb;
    blur += texture2D(uTexture, vTextureCoord + vec2(offset.x, offset.y)).rgb;
    vec3 finalColor = mix(color.rgb, blur / 12.0, uSmoothStrength * 0.4);

    finalColor.r += uGlowStrength * 0.05;
    finalColor.b -= uGlowStrength * 0.02;
    finalColor = mix(finalColor, finalColor * vec3(0.8, 0.2, 0.3) * 1.5, mask.r * uGlowStrength);
    vec3 blush = vec3(1.0, 0.4, 0.4) * mask.g * uGlowStrength * 0.5;
    finalColor = 1.0 - (1.0 - finalColor) * (1.0 - blush);

    float luminance = dot(finalColor, vec3(0.299, 0.587, 0.114));
    finalColor += vec3(max(luminance - 0.65, 0.0) * uGlowStrength * 0.25);
    gl_FragColor = vec4(clamp(finalColor, 0.0, 1.0), color.a);
}
