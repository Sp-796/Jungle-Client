Spec base cliente - Caso 2 POO

Constants (package constants)

	interface IConstants // Centraliza los valores fijos y parametros configurables utilizados por el cliente

    		+ int initialEnergyPlayer = 20 // Energia inicial de cada animal perteneciente al pack de un jugador
		+ int initialEnergyBot = 10 // Energia inicial de cada animal perteneciente a un pack bot
		+ int maxAmountCoffee = 10 // Cantidad total de tazas de cafe disponibles en la partida
		+ int maxEnergy = 20 // Energia maxima que puede alcanzar un animal del pack de un jugador
		+ int maxDamageCapacityPlayer = 12 // Daño maximo teorico del pack de un jugador daño inicial 2 + 10 tazas
		+ int maxDamageCapacityBot = 1 // Daño del pack bot, que no aumenta mediante las tazas de cafe
		+ int initialDamagePlayer = 2 // Daño inicial compartido por los animales del pack de un jugador
		+ int initialDamageBot = 1 // Daño inicial compartido por los animales de un pack bot
		+ int attackCooldown = 1000 // Tiempo minimo en milisegundos entre ataques consecutivos de un animal
		+ int maxTeamSizePlayer = 10 // Cantidad de animales que conforman el pack de un jugador
		+ int maxTeamSizeBot = 4 // Cantidad de animales que conforman cada pack bot
		+ int maxZones = 12 // Cantidad total de zonas que conforman el mapa
		+ int botPackCount = 5 // Cantidad de packs bot presentes en la partida
		+ double animalSpeed // Velocidad constante utilizada para representar el movimiento de los animales
		+ double encounterRadius // Radio utilizado para determinar cuando dos animales se encuentran
		+ int refreshRate // Frecuencia de actualizacion visual del cliente
		+ String defaultServerIP = "localhost" // Direccion IP utilizada por defecto para conectarse al servidor
		+ int defaultServerPort = 8080 // Puerto utilizado por defecto para establecer la conexion con el servidor
		+ int windowWidth = 1280 // Ancho de la ventana del juego en pixeles
		+ int windowHeight = 720 // Alto de la ventana del juego en pixeles
		+ String assetsPath = "/assets/images/" // Ruta base donde se encuentran los recursos graficos del juego

Model (package model)

	class Animal

		+ int id // Identificador unico del animal
		+ String species // Especie del animal utilizada para determinar y mostrar su representacion grafica
		+ int currentEnergy // Energia actual del animal recibida desde el servidor
		+ double x // Posicion horizontal actual del animal en el mapa
		+ double y // Posicion vertical actual del animal en el mapa
		+ Zone currentZone // Zona en la que se encuentra actualmente el animal
		+ boolean isAlive // Indica si el animal continua vivo segun el estado recibido del servidor

	class Zone

		+ String zoneId // Identificador unico de la zona del mapa
		+ List<Path> connectedPaths // Caminos que conectan esta zona con otras zonas del mapa
		+ boolean hasFlag // Indica si existe una bandera en esta zona
		+ boolean isVisible // Indica si la zona puede ser mostrada actualmente al jugador

	class GameState

		+ Map<String, Zone> mapZones // Contiene las zonas conocidas del mapa para representar su estructura
		+ List<Animal> visibleAnimals // Animales que el servidor permite visualizar al jugador
		+ List<String> visibleCoffeeCups // Identificadores o posiciones de las tazas de cafe que el jugador puede visualizar
		+ int aliveAnimalsPlayer // Cantidad de animales vivos pertenecientes al jugador
		+ int deadAnimalsPlayer // Cantidad de animales eliminados del pack del jugador
		+ int botsKilled // Cantidad de animales pertenecientes a packs bot eliminados por el jugador
		+ int coffeeCupsCollected // Cantidad de tazas de cafe consumidas por el jugador
		+ String playerName // Nickname del jugador
		+ String currentZoneId // Identificador de la zona actualmente seleccionada para visualizar
		+ boolean isGameOver // Indica si la partida ha terminado
		+ boolean playerWon // Indica si el jugador obtuvo la victoria cuando la partida finalizo
		+ long matchDuration // Duracion de la partida recibida al finalizar el juego

UI Layer (package ui)

	interface Observer

		+ void update(GameState currentState) // Recibe una actualizacion del estado del juego para refrescar la interfaz

	class MainWindow

		+ MapPanel mapPanel // Panel encargado de representar visualmente el mapa y sus elementos
		+ HUDPanel hudPanel // Panel encargado de mostrar informacion de la partida
		- void initializeWindow() // Configura la ventana utilizando las constantes de tamaño definidas
	
	class MapPanel implements Observer

		- void drawZones(Map<String, Zone> zones) // Dibuja las zonas visibles del mapa
		- void drawPaths(List<Path> connectedPaths) // Dibuja los caminos que conectan las zonas
		- void drawCoffee(List<String> visibleCoffeeCups) // Dibuja las tazas de cafe visibles en sus posiciones correspondientes
		- void drawAnimals(List<Animal> visibleAnimals) // Dibuja los animales visibles utilizando sus posiciones actuales
		+ void update(GameState currentState) // Actualiza la representacion visual utilizando el estado recibido

	class HUDPanel implements Observer

		- void drawScoreboard(int aliveAnimalsPlayer, int botsKilled) // Muestra informacion relacionada con los animales vivos y los bots eliminados
		- void drawCoffeeCounter(int cups) // Muestra la cantidad de tazas de cafe recolectadas por el jugador
		- void showGameOverScreen(boolean isGameOver, boolean playerWon) // Muestra el resultado de la partida cuando esta termina
		+ void update(GameState currentState) // Actualiza la informacion del HUD utilizando el estado recibido

Control Layer (package control)

	class NetworkClient

		- Socket socket // Conexion TCP utilizada para comunicarse con el servidor
		- List<Observer> observers // Observadores que reciben las actualizaciones del estado del juego
		+ void connectToServer(String ip, int port) // Establece la conexion con el servidor
		+ void listenForUpdate() // Ejecuta el hilo encargado de recibir mensajes JSON del servidor
		+ void sendCreateMatch(String nickname) // Envia al servidor la solicitud para crear una nueva partida
		+ void sendJoinMatch(String nickname, String code) // Envia al servidor la solicitud para unirse a una partida existente
		+ void sendSelectAnimal(String species) // Envia al servidor la especie seleccionada por el jugador
		+ void sendMoveAnimals(List<Integer> animalIds, double targetX, double targetY) // Envia al servidor la intencion de mover los animales seleccionados hacia una posicion
		+ void sendAttack(List<Integer> animalIds, int targetAnimalId) // Envia al servidor la intencion de atacar al animal objetivo
		+ void sendViewZone(String zoneId) // Solicita al servidor visualizar una zona en la que el jugador tenga animales vivos
		+ void sendAction(String actionType, Object payload) // Construye y envia un mensaje JSON utilizando un tipo de accion y su respectivo payload

	class InputController

		+ NetworkClient networkClient // Instancia utilizada para enviar las intenciones del jugador al servidor
		+ void onMouseClick(int x, int y) // Detecta la posicion donde el jugador realizo un clic
		- void handleMoveIntent(List<Integer> selectedAnimalIds, double targetX, double targetY) // Envia al servidor la intencion de mover los animales hacia una posicion
		- void handleAttackIntent(List<Integer> selectedAnimalIds, int targetAnimalId) // Envia al servidor la intencion de atacar al animal seleccionado
		- void handleZoneViewIntent(String zoneId) // Solicita al servidor cambiar la zona que se desea visualizar


