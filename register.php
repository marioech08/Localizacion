<?php
$response = array();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (isset($_POST['email']) && isset($_POST['password'])) {

        $email = $_POST['email'];
        $password = $_POST['password'];

        // Conexión a la base de datos
        $conn = new mysqli("localhost", "Xmechavarri003", "HT1twKz1", "Xmechavarri003_usuarios");

        if ($conn->connect_error) {
            $response['success'] = false;
            $response['message'] = "Error de conexión con la base de datos";
        } else {
            // Comprobar si ya existe ese email
            $stmt = $conn->prepare("SELECT email FROM users WHERE email = ?");
            $stmt->bind_param("s", $email);
            $stmt->execute();
            $stmt->store_result();

            if ($stmt->num_rows > 0) {
                $response['success'] = false;
                $response['message'] = "El usuario ya existe";
            } else {
                $stmt->close();

                // Insertar nuevo usuario
                $insert = $conn->prepare("INSERT INTO users (email, password) VALUES (?, ?)");
                $insert->bind_param("ss", $email, $password);

                if ($insert->execute()) {
                    $response['success'] = true;
                    $response['message'] = "Usuario registrado correctamente";
                } else {
                    $response['success'] = false;
                    $response['message'] = "Error al registrar el usuario";
                }

                $insert->close();
            }

            $conn->close();
        }

    } else {
        $response['success'] = false;
        $response['message'] = "Faltan parámetros";
    }

} else {
    $response['success'] = false;
    $response['message'] = "Método no permitido";
}

echo json_encode($response);
?>

