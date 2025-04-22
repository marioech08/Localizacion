<?php
header("Content-Type: application/json");

$conexion = mysqli_connect("localhost", "Xmechavarri003", "HT1twKz1", "Xmechavarri003_usuarios");
if (!$conexion) {
    http_response_code(500);
    echo json_encode(["error" => "❌ Error de conexión"]);
    exit;
}

$resultado = mysqli_query($conexion, "SELECT id, email, latitud, longitud, imagen_nombre FROM fotos");
$datos = [];

while ($fila = mysqli_fetch_assoc($resultado)) {
    $datos[] = $fila;
}

echo json_encode($datos);
mysqli_close($conexion);
?>
