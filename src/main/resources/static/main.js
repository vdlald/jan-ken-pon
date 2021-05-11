let app = undefined
let binding = undefined
let stompClient = undefined

$(async function () {
  app = {
    serverUsername: "",
    stompClient: undefined,
    binding: {
      isOnlineText: $('#isOnlineText'),
      gameRoomIdInput: $('#gameRoomIdInput'),
      usernameInput: $('#usernameInput'),
      passwordInput: $('#passwordInput'),
      createGameRoomButton: $('#createGameRoomButton'),
      connectToGameRoomButton: $('#connectToGameRoomButton'),
      startGameButton: $('#startGameButton'),
      gameStartedText: $('#gameStartedText'),
      playerList: $('#playerList'),
      roundText: {
        element: $('#round'),
        setRound: round => {
          binding.roundText.element.html(`Round: ${round}`)
        }
      },
      readyButton: $('#readyButton'),
      logs: {
        element: $('#log'),
        addLog: message => {
          binding.logs.element.append(createLog(message))
        },
        addGameLog: message => {
          binding.logs.element.append(createLog(message, "game"))
        },
        addRoundResult: message => {
          binding.logs.element.append(createLog(message, "round-result"))
        }
      },
      rockButton: $('#rockButton'),
      paperButton: $('#paperButton'),
      scissorsButton: $('#scissorsButton'),
    },
    data: {
      lastConnection: {
        gameRoomId: "",
        username: "",
        password: ""
      }
    },
    subscriptions: [],
    players: {}
  }
  window.app = app
  binding = app.binding

  connect()
  restoreData()

  binding.createGameRoomButton.on('click', onCreateGameRoomClick)
  binding.connectToGameRoomButton.on('click', onConnectToGameRoomClick)
  binding.startGameButton.on('click', onStartGameClick)
  binding.readyButton.on('click', onReadyButtonClick)
  binding.rockButton.on('click', () => playerChoose("ROCK"))
  binding.paperButton.on('click', () => playerChoose("PAPER"))
  binding.scissorsButton.on('click', () => playerChoose("SCISSORS"))

  binding.readyButton.hide()
})

function connect() {
  const socket = new SockJS('/sock-js')
  stompClient = Stomp.over(socket)

  app.stompClient = stompClient
  binding.isOnlineText.html("You online")

  stompClient.connect({}, function (frame) {
    let serverUsername = frame.headers["user-name"];
    app.serverUsername = serverUsername

    console.log('Connected: ' + frame)
    console.log('username: ' + serverUsername)

    stompClient.subscribe(
        `/user/${serverUsername}/queue/reply/gameRoom.connected`,
        onConnectedToGameRoom
    )
    stompClient.subscribe(
        `/user/${serverUsername}/queue/reply/gameRoom.created`,
        onGameRoomCreated
    )
  })
}

function saveData() {
  localStorage.setItem("app.data", JSON.stringify(window.app.data))
}

function restoreData() {
  let data = JSON.parse(localStorage.getItem("app.data"))
  if (data == null) {
    return
  }
  let binding = window.app.binding
  window.app.data = data

  binding.gameRoomIdInput.val(data.lastConnection.gameRoomId)
  binding.usernameInput.val(data.lastConnection.username)
  binding.passwordInput.val(data.lastConnection.password)
}

// region user listeners

function onConnectedToGameRoom(message) {
  let gameRoom = JSON.parse(message.body).gameRoom;
  console.log(gameRoom)

  // update players list
  gameRoom.players.forEach(
      player => binding.playerList.append(createPlayer(player).element)
  )

  gameRoom.game.readyPlayers.forEach(username => app.players[username].ready())

  // update game started text
  if (gameRoom.game.started) {
    binding.gameStartedText.html("Game started")
    binding.roundText.setRound(gameRoom.game.round)
  } else {
    binding.gameStartedText.html("Game is not started")
    binding.readyButton.show()
  }
}

function onGameRoomCreated(message) {
  let gameRoomId = JSON.parse(message.body).gameRoomId;
  binding.gameRoomIdInput.val(gameRoomId)
}

// endregion
// region broadcast listeners

function onNewPlayer(message) {
  let player = JSON.parse(message.body).player
  if (player.username === app.data.lastConnection.username) {
    return
  }
  binding.playerList.append(createPlayer(player).element)
}

function onExtraRound(message) {
  binding.roundText.setRound("EXTRA")
  let usernames = JSON.parse(message.body).usernames
  binding.logs.addGameLog("EXTRA ROUND")
  if (usernames.includes(app.data.lastConnection.username)) {
    binding.logs.addGameLog("you participate")
  } else {
    binding.logs.addLog("you don't participate")
  }
}

function onGameFinished(message) {
  let response = JSON.parse(message.body)
  let winner = response.winner
  let roundsWinners = response.roundsWinners
  binding.logs.addRoundResult(`Game winner is: ${winner}`)
  console.log(roundsWinners)
  app.subscriptions.forEach(s => s.unsubscribe())
}

function onNextRound(message) {
  let round = JSON.parse(message.body).round;
  binding.roundText.setRound(round)
}

function onPlayerChosen(message) {
  let username = JSON.parse(message.body).username;
  binding.logs.addLog(`${username} has chosen`)
}

function onPlayerReady(message) {
  let username = JSON.parse(message.body).username;
  app.players[username].ready()
}

function onRoundWinner(message) {
  let response = JSON.parse(message.body);
  let winner = response.winner;
  let playersChoices = response.playersChoices;
  playersChoices.forEach(
      pair => binding.logs.addLog(`User ${pair.first} chose ${pair.second}`)
  )
  binding.logs.addRoundResult(`round winner: ${winner}`)
}

function onGameStarted() {
  binding.gameStartedText.html("Game started")
}

function onDraw(message) {
  let response = JSON.parse(message.body);
  let usernames = response.usernames;
  let playersChoices = response.playersChoices;
  playersChoices.forEach(
      pair => binding.logs.addLog(`User ${pair.first} chose ${pair.second}`)
  )
  binding.logs.addRoundResult("Draw!")
  if (usernames.includes(app.data.lastConnection.username)) {
    binding.logs.addGameLog("you participate")
  } else {
    binding.logs.addLog("you don't participate")
  }
  console.log(playersChoices)
}

// endregion
// region ui listeners

function onCreateGameRoomClick() {
  createGameRoom(3)
}

function onConnectToGameRoomClick() {
  let username = binding.usernameInput.val();
  let password = binding.passwordInput.val();
  let gameRoomId = binding.gameRoomIdInput.val();

  app.data.lastConnection.username = username
  app.data.lastConnection.password = password
  app.data.lastConnection.gameRoomId = gameRoomId
  saveData()

  app.subscriptions.some(s => s.unsubscribe())

  connectToGameRoom(username, password, gameRoomId)

  app.subscriptions.push(
      stompClient.subscribe(
          `/topic/${gameRoomId}/gameRoom.newPlayer`,
          onNewPlayer
      ),
      stompClient.subscribe(
          `/topic/${gameRoomId}/game.extraRound`,
          onExtraRound
      ),
      stompClient.subscribe(
          `/topic/${gameRoomId}/game.finish`,
          onGameFinished
      ),
      stompClient.subscribe(
          `/topic/${gameRoomId}/game.nextRound`,
          onNextRound
      ),
      stompClient.subscribe(
          `/topic/${gameRoomId}/player.chosen`,
          onPlayerChosen
      ),
      stompClient.subscribe(
          `/topic/${gameRoomId}/player.ready`,
          onPlayerReady
      ),
      stompClient.subscribe(
          `/topic/${gameRoomId}/game.round.winner`,
          onRoundWinner
      ),
      stompClient.subscribe(
          `/topic/${gameRoomId}/game.started`,
          onGameStarted
      ),
      stompClient.subscribe(
          `/topic/${gameRoomId}/game.round.draw`,
          onDraw
      ),
  )
}

function onStartGameClick() {
  startGame()
}

function onReadyButtonClick() {
  playerReady()
  binding.readyButton.hide()
}

// endregion
// region GameRoom API

function createGameRoom(maxRounds) {
  stompClient.send(
      `/api/gameRoom.create`,
      {},
      JSON.stringify({maxRounds: maxRounds})
  )
}

function connectToGameRoom(username, password, gameRoomId) {
  stompClient.send(
      `/api/${gameRoomId}/gameRoom.connect`,
      {"authorization": btoa(`${username}:${password}`)},
      ''
  )
}

function playerReady() {
  let username = app.data.lastConnection.username;
  let password = app.data.lastConnection.password;
  let gameRoomId = app.data.lastConnection.gameRoomId;

  stompClient.send(
      `/api/${gameRoomId}/player.ready`,
      {"authorization": btoa(`${username}:${password}`)},
      ''
  )
}

function startGame() {
  let username = app.data.lastConnection.username;
  let password = app.data.lastConnection.password;
  let gameRoomId = app.data.lastConnection.gameRoomId;

  stompClient.send(
      `/api/${gameRoomId}/game.start`,
      {"authorization": btoa(`${username}:${password}`)},
      ''
  )
}

function playerChoose(choice) {
  let username = app.data.lastConnection.username;
  let password = app.data.lastConnection.password;
  let gameRoomId = app.data.lastConnection.gameRoomId;

  stompClient.send(
      `/api/${gameRoomId}/player.choose`,
      {"authorization": btoa(`${username}:${password}`)},
      JSON.stringify({choice: choice})
  )
}

// endregion
// region elements

function createPlayer(player) {
  let tr = document.createElement("tr");
  let username = document.createElement("td");
  let color = document.createElement("td");

  tr.append(color)
  tr.append(username)

  username.innerHTML = player.username
  username.style.borderRight = "2px"
  username.style.borderRightStyle = "solid"
  username.style.borderRightColor = "red"

  color.style.backgroundColor = player.color

  let playerObject = {
    element: tr,
    username: player.username,
    ready: () => {
      username.style.borderRightColor = "green"
    }
  };
  app.players[player.username] = playerObject

  return playerObject
}

function createLog(message, level = "") {
  let div = document.createElement("div");
  div.className = "row log-" + level
  div.innerHTML = message
  return div
}

// endregion