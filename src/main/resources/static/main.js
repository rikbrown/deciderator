'use strict';

class DecideratorApp {
    constructor() {
        this.setHandler(SignInSocketHandler);

        let webSocket = this.webSocket = new SockJS('/handler');
        webSocket.onopen = () => console.log('Session open');
        webSocket.onmessage = (msg) => this.handler.receiveMessage(msg);
        webSocket.onclose = () => console.log('close');
    }

    setHandler(handlerClass) {
        this.handler = new handlerClass(this);
        this.updateUi();
    }

    updateUi() {
        this.handler.initUi();
    }
}

class SocketHandler {
    constructor(app) {
        this.app = app
    }

    getHandlers() {
        return {}
    }

    initUi() {
        $('#rows-container>.row.page').hide()
    }

    receiveMessage(msg) {
        const data = JSON.parse(msg.data);
        const messageClass = data['@class'].replace('codes.rik.deciderator.types.', '')
        const handler = this.getHandlers()[messageClass];

        if (handler) {
            handler.bind(this)(data);
        } else {
            console.warn("Unexpected message", data['@class'], data);
        }
    }

    sendMessage(type, msg) {
        msg['@class'] = 'codes.rik.deciderator.types.' + type;
        this.app.webSocket.send(JSON.stringify(msg))
    }

}

class SignInSocketHandler extends SocketHandler {
    getHandlers() {
        return {
            'HelloMessage': this.helloHandler,
            'UsernameSetMessage': this.usernameSetHandler,
        };
    }

    initUi() {
        super.initUi();
        $('#row-sign-in').show();
        $('#form-signin').unbind('submit').submit((e) => {
            e.preventDefault();
            $('#form-signin>fieldset').prop('disabled', true);
            this.setUsername($('#username').val());
        });
    }

    // handlers

    helloHandler(data) {
        $('.session-id')
            .text(data.sessionId)
            .prop('title', data.onlineSessionIds.join(', '))
    }

    usernameSetHandler() {
        console.log("Username was set");
        this.app.setHandler(JoinUncertaintySocketHandler);
    }

    // actions

    setUsername(username) {
        console.log("Setting username", username);
        this.sendMessage('SetUsernameRequest', {
            username: username
        });
    }
}

class JoinUncertaintySocketHandler extends SocketHandler {
    getHandlers() {
        return {
            'UncertaintyJoinedMessage': this.joinOrCreateHandler,
            'UncertaintyCreatedMessage': this.joinOrCreateHandler,
            'UncertaintyNotFoundMessage': errorMessageHandler
        }
    }
    initUi() {
        super.initUi();
        $('#row-join').show();

        $('#form-join').unbind('submit').submit((e) => {
            e.preventDefault();
            $('#form-join>fieldset').prop('disabled', true);
            this.joinUncertainty($('#uncertaintyId').val());
        });
        $('#btn-new-uncertainty').unbind('click').click((e) => {
            e.preventDefault();
            $('#form-join>fieldset').prop('disabled', true);
            this.createUncertainty()
        })
    }

    // actions

    createUncertainty() {
        this.sendMessage('CreateUncertaintyRequest', {})
    }

    joinUncertainty(uncertaintyId) {
        this.sendMessage('JoinUncertaintyRequest', { uncertaintyId: uncertaintyId })
    }

    // handlers

    joinOrCreateHandler(data) {
        console.log("Uncertainty joined/created", data.uncertaintyId)
    }

}

let app = new DecideratorApp();

$('.row.page').hide();
$('#row-main').show();

// receive


function echoHandler(data) {
    console.log("echo", data);
}

function errorMessageHandler(data) {
    alert(data.error);
    location.reload();
}

