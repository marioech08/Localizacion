<?php
// Mostrar errores en pantalla
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Leer JSON crudo
$input = json_decode(file_get_contents("php://input"), true);

// Verificar que se reciben todos los campos
if (!isset($input["email"], $input["imagen"], $input["latitud"], $input["longitud"])) {
    http_response_code(400);
    echo json_encode(["success" => false, "error" => "Faltan campos"]);
    exit;
}

// Recoger los datos
$email = $input["email"];
$imagen = $input["imagen"];
$latitud = floatval($input["latitud"]);
$longitud = floatval($input["longitud"]);

$imagen_nombre = uniqid() . ".jpg";
$carpeta = "uploads/";

// Asegurar que la carpeta existe
if (!file_exists($carpeta)) {
    mkdir($carpeta, 0777, true);
}

// Guardar imagen
$ruta_imagen = $carpeta . $imagen_nombre;
$guardado = file_put_contents($ruta_imagen, base64_decode($imagen));

if ($guardado === false) {
    http_response_code(500);
    echo json_encode(["success" => false, "error" => "Error guardando imagen"]);
    exit;
}

// Conectar con la base de datos
$conexion = mysqli_connect("localhost", "Xmechavarri003", "HT1twKz1", "Xmechavarri003_usuarios");
if (!$conexion) {
    http_response_code(500);
    echo json_encode(["success" => false, "error" => "Error de conexión BBDD"]);
    exit;
}

// Insertar en la base de datos
$consulta = "INSERT INTO fotos (email, imagen_nombre, latitud, longitud) VALUES (?, ?, ?, ?)";
$stmt = $conexion->prepare($consulta);

if (!$stmt) {
    http_response_code(500);
    echo json_encode(["success" => false, "error" => "Error en prepare()"]);
    exit;
}

$stmt->bind_param("ssdd", $email, $imagen_nombre, $latitud, $longitud);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "Imagen subida correctamente"]);
} else {
    http_response_code(500);
    echo json_encode(["success" => false, "error" => "Error al guardar en la base de datos"]);
}

$stmt->close();
$conexion->close();
?>
