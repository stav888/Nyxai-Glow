#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES uTexture;
uniform sampler2D uMakeupMask;
uniform float uGlowStrength;
uniform float uSmoothStrength;
uniform vec2 uTexelSize;
varying vec2 vTextureCoord;

// TODO: Add a dedicated skin channel when the mask generator exposes one.
// Keep smoothing face-limited without guessing which existing makeup channel is skin.

void main() {
    vec4 color = texture2D(uTexture, vTextureCoord);
    vec4 mask = texture2D(uMakeupMask, vTextureCoord);
    vec2 offset = uTexelSize;
    vec3 smoothColor = color.rgb * 4.0;
    smoothColor += texture2D(uTexture, vTextureCoord + vec2(-offset.x, -offset.y)).rgb;
    smoothColor += texture2D(uTexture, vTextureCoord + vec2(0.0, -offset.y)).rgb;
    smoothColor += texture2D(uTexture, vTextureCoord + vec2(offset.x, -offset.y)).rgb;
    smoothColor += texture2D(uTexture, vTextureCoord + vec2(-offset.x, 0.0)).rgb;
    smoothColor += texture2D(uTexture, vTextureCoord + vec2(offset.x, 0.0)).rgb;
    smoothColor += texture2D(uTexture, vTextureCoord + vec2(-offset.x, offset.y)).rgb;
    smoothColor += texture2D(uTexture, vTextureCoord + vec2(0.0, offset.y)).rgb;
    smoothColor += texture2D(uTexture, vTextureCoord + vec2(offset.x, offset.y)).rgb;
    float makeupCoverage = smoothstep(0.02, 0.25, max(mask.r, mask.g));
    vec3 finalColor = mix(color.rgb, smoothColor / 12.0, uSmoothStrength * 0.28 * makeupCoverage);

    vec3 warmTone = vec3(1.0, 0.985, 0.96);
    finalColor *= mix(vec3(1.0), warmTone, 0.12 * uGlowStrength);
    finalColor += vec3(0.035, 0.012, 0.0) * uGlowStrength;
    finalColor = mix(finalColor, finalColor * vec3(0.8, 0.2, 0.3) * 1.5, mask.r * uGlowStrength);
    vec3 blush = vec3(1.0, 0.4, 0.4) * mask.g * uGlowStrength * 0.5;
    finalColor = 1.0 - (1.0 - finalColor) * (1.0 - blush);

    float luminance = dot(finalColor, vec3(0.299, 0.587, 0.114));
    finalColor += vec3(max(luminance - 0.65, 0.0) * uGlowStrength * 0.25);
    gl_FragColor = vec4(clamp(finalColor, 0.0, 1.0), color.a);
}
