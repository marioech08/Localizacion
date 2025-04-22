<?php
$conexion = new mysqli("localhost", "Xmechavarri003", "HT1twKz1", "Xmechavarri003_usuarios2");

if ($conexion->connect_error) {
    die("Connection failed: " . $conexion->connect_error);
}
?>
