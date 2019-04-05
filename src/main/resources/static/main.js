'use strict';

class DecideratorApp {
    constructor() {
        let webSocket = this.webSocket = new SockJS('/handler');
        webSocket.onmessage = (msg) => this.handler.receiveMessage(msg);
        webSocket.onclose = () => {
            errorMessageHandler({error: 'The connection was closed by the server because it sucks'})
        };
        webSocket.onopen = () => {
            console.log('Session open');
            this.setHandler(new SignInSocketHandler(this));
        }
    }

    setHandler(handler) {
        this.handler = handler;
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

    allHandlers() {
        return {
            ...(this.getHandlers()),
            "UncertaintyActiveUsersMessage": this.handleActiveUsers
        }
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
        const handler = this.allHandlers()[messageClass];

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

    handleActiveUsers(data) {
        const $users = $('.online-users')
            .empty();
        data.users.forEach(user => {
            $users.append($('<span>').addClass('badge badge-success').text(user.name))
        });
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

        $('#username').focus()

        const savedUsername = window.localStorage.getItem('username');
        if (savedUsername) {
            $('#username').val(savedUsername);
            $('#username').submit();
        }
    }

    // handlers

    helloHandler(data) {
        $('.session-id')
            .text(data.sessionId)
            .prop('title', data.onlineSessionIds.join(', '))
    }

    usernameSetHandler() {
        console.log("Username was set");
        this.app.setHandler(new JoinUncertaintySocketHandler(this.app));
    }

    // actions

    setUsername(username) {
        console.log("Setting username", username);
        window.localStorage.setItem('username', username)
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
        });

        const locationId = location.hash.replace('#', '')
        if (locationId) {
            $('#uncertaintyId').val(locationId);
            $('#form-join ').submit();
        }

        $('#uncertaintyId').focus()
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
        console.log("Uncertainty joined/created", data.uncertaintyId);
        this.app.setHandler(new ViewUncertaintyHandler(this.app, data.uncertaintyId, data.info));
    }
}

class ViewUncertaintyHandler extends SocketHandler {
    constructor(app, uncertaintyId, uncertaintyInfo) {
        super(app);
        this.uncertaintyId = uncertaintyId;
        this.uncertaintyInfo = uncertaintyInfo;
        this.flipping = false;
    }

    getHandlers() {
        return {
            'DecidingMessage': this.decidingHandler,
            'DecisionMessage': this.decisionHandler,
            'UncertaintyUpdatedMessage': this.uncertaintyUpdatedHandler,
        }
    }

    initUi() {
        super.initUi();

        document.location.replace('#' + this.uncertaintyId);

        let coin = this.coin = new Coin3D($('#threejs-container'), 300, 300, this.uncertaintyInfo.coinStyle.toLowerCase());
        coin.setup();
        coin.render();
        coin.enableControls();

        $('#threejs-container').mousedown((e) => {
            if (e.which == 3 && !this.flipping && !this.hasDecision) {
                this.setCoinStyle(this.uncertaintyInfo.coinStyle == "GERMANY" ? "FIRST_WORLD_WAR" : "GERMANY", true);
            }
        });

        this.updateDecisions(this.uncertaintyInfo.decisions);

        $('#row-view').show();
        $('#btn-flip').unbind('click').click((e) => {
            e.preventDefault();

            this.startFlip()
        });

        $('.uncertainty-id').text(this.uncertaintyId);
        $('.uncertainty-name').text(this.uncertaintyInfo.name);

        $('#btn-edit-name').unbind('click').click((e) => {
            e.preventDefault();

            const text = $('span.uncertainty-name').text();
            const $span = $('span.uncertainty-name')
                .attr('contenteditable', true)
                .text(text ? text : ' ');

            const selection = window.getSelection();
            const range = document.createRange();
            range.selectNodeContents($span[0]);
            selection.removeAllRanges();
            selection.addRange(range);

            $('#btn-edit-name').hide();
            $('#btn-save-name').css('display', 'inline');

            coin.disableControls()
        });

        $('#btn-save-name').unbind('click').click((e) => {
            const newName = $('span.uncertainty-name').text();
            this.setName(newName);

            coin.enableControls()
        });

        $('span.uncertainty-name').unbind('keypress').keypress((e) => {
            if (e.which === 13) {
                e.preventDefault()
                $('#btn-save-name').click()
            }
        });
    }

    updateName(name) {
        this.uncertaintyInfo.name = name;
        $('span.uncertainty-name')
            .text(name)
            .attr('contenteditable', false);
        $('#btn-save-name').hide();
        $('#btn-edit-name').css('display', 'inline');
    }

    updateDecisions(decisions) {
        $('.coin-col .coins').empty();
        decisions.forEach(decision => {
            this.hasDecision = true;
            $('.coin-col.' + decision.toLowerCase() + ' .coins')
                .append($('<img/>').attr('src', 'coins/' + this.uncertaintyInfo.coinStyle.toLowerCase() + '/' + decision.toLowerCase() + '.png'))
                .append($('<br/>'))
        })
    }

    // Commands

    startFlip() {
        $('#btn-flip')
            .text('Flipping...')
            .prop('disabled', true);

        this.coin.prepareForFlip();
        this.sendMessage('MakeDecisionRequest', { uncertaintyId: this.uncertaintyId });
    }

    setName(name) {
        this.updateName(name);
        this.sendMessage('SetUncertaintyNameRequest', {
            uncertaintyId: this.uncertaintyId,
            name: name });
    }

    setCoinStyle(newCoinStyle, save) {
        this.coin.setCoinStyle(newCoinStyle.toLowerCase());
        this.coin.setup();
        this.coin.enableControls();
        this.uncertaintyInfo.coinStyle = newCoinStyle;

        if (save) {
            this.sendMessage('SetUncertaintyCoinStyleRequest', {
                uncertaintyId: this.uncertaintyId,
                coinStyle: newCoinStyle
            });
        }
    }

    // Handlers

    uncertaintyUpdatedHandler(data) {
        if (data.uncertaintyId !== this.uncertaintyId) return;

        this.updateName(data.info.name);
        this.updateDecisions(data.info.decisions);

        if (data.info.coinStyle != this.uncertaintyInfo.coinStyle) {
            this.setCoinStyle(data.info.coinStyle, false)
        }
    }

    decidingHandler(data) {
        if (data.uncertaintyId !== this.uncertaintyId) return;

        if (!this.flipping) {
            $('#btn-flip')
                .text('Flipping...')
                .prop('disabled', true);

            this.coin.prepareForFlip();
            this.flipping = true
        }

        let coin = this.coin;
        coin.animationMethod = () => {
            coin.object.rotation.x += data.rotation.x;
            coin.object.rotation.y += data.rotation.y;
            coin.object.rotation.z += data.rotation.z;
        }
    }

    decisionHandler(data) {
        if (data.uncertaintyId !== this.uncertaintyId) return;
        console.log(data);

        let coin = this.coin;
        coin.prepareForFlip();

        if (data.decision === 'TAILS') {
            coin.object.rotation.z = 1
        } else {
            coin.object.rotation.z = 2;
            coin.object.rotation.y = -1.5;
        }

        coin.enableControls();

        $('#btn-flip')
            .text('Flip!')
            .prop('disabled', false)

        $('.coin-col.' + data.decision.toLowerCase())
            .append($('<img/>').attr('src', 'coins/' + this.uncertaintyInfo.coinStyle.toLowerCase() + '/' + data.decision.toLowerCase() + '.png'))
            .append($('<br/>'))

        this.flipping = false
    }

}

let app = new DecideratorApp();

// $('.row.page').hide();
// $('#row-main').show();


// receive


function echoHandler(data) {
    console.log("echo", data);
}

function errorMessageHandler(data) {
    alert(data.error);
    location.hash = ''
    location.reload()
}

