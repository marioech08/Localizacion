<?php
// Conectar a la base de datos
$conn = new mysqli("localhost", "Xmechavarri003", "HT1twKz1", "Xmechavarri003_usuarios");
$resultado = $conn->query("SELECT * FROM fotos");
$fotos = [];
while ($fila = $resultado->fetch_assoc()) {
    $fotos[] = $fila;
}
$conn->close();
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Mapa de imágenes</title>
    <style>
        #map { height: 100vh; width: 100%; }
        body { margin: 0; }
    </style>
    <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCoJJKd7AwiqKbwWlX_XKute89wKComvuc"></script>
    <script>
        const fotos = <?php echo json_encode($fotos); ?>;

        function initMap() {
            const centro = { lat: 40.4168, lng: -3.7038 }; // Madrid por defecto
            const map = new google.maps.Map(document.getElementById("map"), {
                zoom: 6,
                center: centro
            });

            fotos.forEach(foto => {
                const marker = new google.maps.Marker({
                    position: { lat: parseFloat(foto.latitud), lng: parseFloat(foto.longitud) },
                    map: map,
                    title: foto.email
                });

                const info = new google.maps.InfoWindow({
                    content: `
                        <strong>${foto.email}</strong><br>
                        <img src="uploads/${foto.imagen_nombre}" width="200">
                    `
                });

                marker.addListener("click", () => {
                    info.open(map, marker);
                });
            });
        }

        window.onload = initMap;
    </script>
</head>
<body>
    <div id="map"></div>
</body>
</html>
