varying vec4 v_color;
varying vec2 v_texCoord0;

uniform sampler2D u_texture;
uniform vec4 u_flasher;

void main(void) {
	vec4 color = texture2D(u_texture, v_texCoord0) * v_color;
	color.rgb += u_flasher.rgb;
	gl_FragColor = color;
}
