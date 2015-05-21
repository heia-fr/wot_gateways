$(document).ready(function() {
	// on récupère la valeur du X-Token stockée dans le cookie
	var xToken =  $.cookie("knxwot");
	
	// fonction permettant d'envoyer le mot de passe saisi par l'utilisateur et d'en récuèérer un X-Token
	// entrée : password
	// sortie : X-Token
	$('#sendPassword').click(function() {
		var password = $('#password').val();
		$.ajax({
			type: 'POST',
			url: '/admin/login',
			data: { password: password },
			success: function(data, status, response) {
				// Création d'un cookie (valeur : X-Token, expiration: 1 jour, chemin: /)
				$.cookie("knxcookie", response.getResponseHeader('X-Token'), { expires: 1, path: '/'});
				// Redirection vers la page de config
				window.location = "configuration.html";
			},
			error: function () {
				alert('The authentification has failed');
				$('#password').val('');
			},		
		});
	});
	
	// idem que la fonction précédente, mais lors de la pression sur  la touche enter
	$('#password').keyup(function (event) {
		var key = event.keyCode || event.which;
		if (key == 13) {
			var password = $('#password').val();
			$.ajax({
				type: 'POST',
				url: '/admin/login',
				data: { password: password },
				success: function(data, status, response) {
					$.cookie("knxcookie", response.getResponseHeader('X-Token'), { expires: 1, path: '/'});
					window.location = "configuration.html";
				},
				error: function () {
					alert('The authentification has failed');
					$('#password').val('');
				},		
			});
        }
		return false;
	});
	
	// fonction permettant de se déconnecter (suppression du cookie)
	$('li a#logout').click(function() {
		// on supprime le cookie
		$.removeCookie('knxcookie', { path: '/' });
		window.location = "index.html";
	});
	
	// en fonction de l'état de la case à cocher, on applique un "disabled" sur les champs de connexion pour la DB
	$('#dataStorage').click(function() {
		if ($('#dataStorage').is(':checked')) {
			$('#databaseUser').prop('disabled', false);
			$('#databasePassword').prop('disabled', false);
		} else {
			$('#databaseUser').prop('disabled', true);
			$('#databasePassword').prop('disabled', true);
		};
	});
	
	
	$('#ipGateway').change(function() {
		var ipValue = $(this).val();
		// si égal Ã  1 --> l'input manual ip devient utilisable (sinon, il est bloqué)
		if (ipValue == 1) {
			$('#manualIpGateway').prop('disabled', false);
		} else {
			$('#manualIpGateway').prop('disabled', true);
		};
	});
});