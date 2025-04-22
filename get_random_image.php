<?php
header("Content-Type: application/json");
$conexion = mysqli_connect("localhost", "Xmechavarri003", "HT1twKz1", "Xmechavarri003_usuarios");

$query = "SELECT imagen_nombre FROM fotos ORDER BY RAND() LIMIT 1";
$resultado = mysqli_query($conexion, $query);

$fila = mysqli_fetch_assoc($resultado);

echo json_encode($fila);
?>
