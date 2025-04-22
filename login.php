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
            $stmt = $conn->prepare("SELECT * FROM users WHERE email = ? AND password = ?");
            $stmt->bind_param("ss", $email, $password);
            $stmt->execute();
            $result = $stmt->get_result();

            if ($result->num_rows > 0) {
                $response['success'] = true;
                $response['message'] = "Inicio de sesión exitoso";
            } else {
                $response['success'] = false;
                $response['message'] = "Credenciales incorrectas";
            }

            $stmt->close();
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
