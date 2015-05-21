var PROJECT_NAME = '';

// Verify the username & password of the user and generate the cookies
// (username, isAdmin & X-Token)
function login() {
	// create an user
	var user = {
		username : $('#usrUsername').val(),
		password : $('#usrPassword').val()
	};

	var x_token = 0; // will contain the generated X-Token

	$.ajax({
		type : 'POST',
		url : PROJECT_NAME + '/rest/login',
		contentType : 'application/json',
		data : JSON.stringify(user),
		success : function(data, status, response) {
			// store the X-Token from the response
			var x_token = response.getResponseHeader('X-Token');
			// get the user infos
			$.ajax({
				type : 'GET',
				url : PROJECT_NAME + '/rest/user/username/'
						+ $('#usrUsername').val(),
				success : function(data, status, response) {
					// create the cookies
					// store username
					$.cookie("EnoUsr", $('#usrUsername').val(), {
						expires : 1,
						path : '/'
					});
					// store statut (true = admin, false = user)
					$.cookie("EnoIsAd", data.admin, {
						expires : 1,
						path : '/'
					});
					// store the X-Token
					$.cookie("EnoToken", x_token, {
						expires : 1,
						path : '/'
					});
					// redirection to the configuration page
					window.location = "configuration.html";
				},
				error : function(response, status, error) {
				},
			});
		},
		error : function(response, status, error) {
			showAlert('#alertLoginError'); // show the error alert
			$('#usrPassword').val(''); // erase the content of the password
			// input
		},
	});
}

function logout() {
	$.ajax({
		type : 'POST',
		url : PROJECT_NAME + '/rest/logout',
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			window.location = "index.html";
		},
		error : function(response, status, error) {
		},
	});
}

// Show the given alert during a certain time
// Param : an alert id (with "#")
function showAlert(id) {
	$('' + id + '').show(); // show the alert
	setTimeout(function() {
		$('' + id + '').hide();
	}, 2500); // time in ms (2.5 s.)
}

// Check if th given JSON object is empty or not
// Param : A JSON array
// Return : true (empty) / false (not empty)
function isEmptyObject(object) {
	if (JSON.stringify(object) == '[]') {
		return true;
	} else {
		return false;
	}
}

// Get the EnOcean configuration
// Return : DNS zone & DNS IP via the predefined inputs
function getConfig() {
	$.ajax({
		type : 'GET',
		url : PROJECT_NAME + '/rest/config',
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			// return the value via the predefined inputs
			$('#dnsZone').val(data.DNSZone);
			$('#dns').val(data.DNSZone);
			$('#dnsIp').val(data.DNSIP);
		},
		error : function(response, status, error) {
			showAlert('#alertConfigError');
		},
	});
}

// Enable the configuration inputs
function editConfig() {
	// enable the inputs
	$('#dnsIp').prop('disabled', false);
	$('#dnsZone').prop('disabled', false);

	// add the buttons "Cancel" and "Save changes"
	var output = '';
	output += '<div class="btn-group">';
	output += '<button type="button" class="btn" onClick="cancelConfig();">Cancel</button>';
	output += '<button type="button" class="btn btn-success" onClick="updateConfig();">Save changes</button>';
	output += '</div>';
	$('#configButtons').html(output);
}

// Disable the configuration inputs
function cancelConfig() {
	// disable the buttons
	$('#dnsIp').prop('disabled', true);
	$('#dnsZone').prop('disabled', true);

	// add the button "Edit"
	var output = '';
	output += '<button class="btn" type="button" onClick="editConfig();">Edit</button>';
	$('#configButtons').html(output);

	getConfig(); // reload the config
}

// Update the EnOcean configuration
// Param : dns/one & dnsIp (configuration inputs)
function updateConfig() {
	// create a "configuration" with the inputs data
	var config = {
		DNSZone : $('#dnsZone').val(),
		DNSIP : $('#dnsIp').val()
	};

	$.ajax({
		type : 'POST',
		url : PROJECT_NAME + '/rest/config',
		contentType : 'application/json',
		data : JSON.stringify(config),
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		// statusCode: {
		// 401: function () {
		// window.location = "index.html";
		// },
		// 500: function () {
		// window.location = "index.html";
		// },
		// },
		success : function(data, status, response) {
			showAlert('#alertConfigSuccess');
			cancelConfig(); // restore the inputs as disabled
		},
		error : function(response, status, error) {
			showAlert('#alertConfigError');
		},
	});
}

// Get the registered groups
// Return : a list of group's <option>
function getGroups() {
	$.ajax({
		type : 'GET',
		url : PROJECT_NAME + '/rest/groups',
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			var isEmpty = isEmptyObject(data); // check is the object is empty
			// or now

			if (isEmpty == true) {
				showAlert('#alertNoGroupError'); // if empty, we show the
				// error alert
			} else {
				// generate a list of <option> with the group's id as id and the
				// group's name as value
				var output = "";
				$.each(data, function(key, value) {
					output += '<option id="' + value.id + '">' + value.name
							+ '</option>';
				});
				$('#groups').html(output);
			}
		},
		error : function(response, status, error) {
			showAlert('#alertGroupError');
		},
	});
}

// Get the specified group's information
// Param : group id (comes from the hidden input "groupId"
// Return : the group's information via the predefined inputs
function getGroup() {
	$
			.ajax({
				type : 'GET',
				url : PROJECT_NAME + '/rest/group/' + $('#groupId').val(),
				headers : {
					'X-Token' : $.cookie('EnoToken')
				},
				statusCode : {
					401 : function() {
						window.location = "index.html";
					},
					500 : function() {
						window.location = "index.html";
					},
				},
				success : function(data, status, response) {
					var lastModification = data.lastModification; // get the
					// last
					// modification's
					// data for
					// the
					// spliting
					// operatons

					// set the inputs with the given group's information
					$('#groupName').val(data.name);
					$('#groupDescription').val(data.description);
					$('#groupLastModification').val(
							lastModification.slice(8, 10) + '.'
									+ lastModification.slice(5, 7) + '.'
									+ lastModification.slice(0, 4) + ' by '
									+ data.lastModifier);

					// create the buttons "Add sensor", "Edit" & "Delete"
					var output = '';
					output += '<div class="btn-group">';
					output += '<button type="button" class="btn" onClick="viewSensor();">Add sensor</button>';
					output += '<button type="button" class="btn" onClick="editGroup();">Edit</button>';
					output += '<button type="button" class="btn btn-danger" onClick="deleteGroup();">Delete</button>';
					output += '</div>';
					$('#groupButtons').html(output);
				},
				error : function(response, status, error) {
					showAlert('#alertGroupError');
				},
			});
}

// Create a new group
function addGroup() {
	// create a "group" with input's data
	var group = {
		name : $('#addGroupName').val(),
		description : $('#addGroupDescription').val(),
		lastModifier : $.cookie('EnoUsr')
	};

	$.ajax({
		type : 'PUT',
		url : PROJECT_NAME + '/rest/group',
		contentType : 'application/json',
		data : JSON.stringify(group),
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			showAlert('#alertGroupSuccess'); // show the success alert
			getGroups(); // refresh the group's list

			// erase the content of the group's inputs (pop-up)
			$('#addGroupName').val('');
			$('#addGroupDescription').val('');
		},
		error : function(response, status, error) {
			showAlert('#alertGroupError');
		},
	});
}

// Show the "Add sensor" pop-up
function viewSensor() {
	// $('#sensorId').val(0); // set to 0 --> create a new sensor
	getEepRorgs(); // load the eeps rorg

	// generate the list of manufacturers
	$.ajax({
		type : 'GET',
		url : PROJECT_NAME + '/rest/manufacturers',
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			var output = "";
			$.each(data, function(key, value) {
				output += '<option value="' + value.name + '">' + value.name
						+ '</option>';
			});
			$('#sensorManufacturer' + $('#sensorId').val() + '').html(output);
		},
		error : function(response, status, error) {
		},
	});
	$('#addSensor').modal('show'); // show the pop-up
}

// Enable the group's inputs
function editGroup() {
	// enable the inputs
	$('#groupName').prop('disabled', false);
	$('#groupDescription').prop('disabled', false);

	// add the buttons "Cancel" & "Save changes"
	var output = '';
	output += '<div class="btn-group">';
	output += '<button type="button" class="btn" onClick="cancelGroup();">Cancel</button>';
	output += '<button type="button" class="btn btn-success" onClick="updateGroup();">Save changes</button>';
	output += '</div>';
	$('#groupButtons').html(output);
}

// Update the specified group
// Param : group id (comes from the hidden input "groupId"
function updateGroup() {
	// create a "group" with the input's data
	var group = {
		name : $('#groupName').val(),
		description : $('#groupDescription').val(),
		lastModifier : $.cookie('EnoUsr')
	};

	$.ajax({
		type : 'POST',
		url : PROJECT_NAME + '/rest/group/' + $('#groupId').val(),
		contentType : 'application/json',
		data : JSON.stringify(group),
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			showAlert('#alertGroupSuccess'); // show the success alert
			cancelGroup(); // disable the group's inputs

			// erase the content of the DIVs
			$('#sensors').html('');
			$('#groupLegend').html('');
		},
		error : function(response, status, error) {
			showAlert('#alertGroupError');
		},
	});
}

// Disable the group's inputs
function cancelGroup() {
	// disable the inputs
	$('#groupName').prop('disabled', true);
	$('#groupDescription').prop('disabled', true);

	// create the buttons "Add sensor", "Edit" & "Delete"
	var output = '';
	output += '<div class="btn-group">';
	output += '<button type="button" class="btn" onClick="viewSensor();">Add sensor</button>';
	output += '<button type="button" class="btn" onClick="editGroup();">Edit</button>';
	output += '<button type="button" class="btn btn-danger" onClick="deleteGroup();">Delete</button>';
	output += '</div>';
	$('#groupButtons').html(output);

	getGroups(); // refresh the group's list
}

// Delete the specified group
// Param : group id (comes from the hidden input "groupId"
function deleteGroup() {
	$.ajax({
		type : 'DELETE',
		url : PROJECT_NAME + '/rest/group/' + $('#groupId').val(),
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			// initialization of the inputs
			$('#groupName').val('');
			$('#groupDescription').val('');
			$('#groupLastModification').val('');

			// erase the content of the DIVs
			$('#sensors').html('');
			$('#groupLegend').html('');

			getGroups(); // refresh the groups' list
			showAlert('#alertGroupSuccess');
		},
		error : function(response, status, error) {
			showAlert('#alertGroupError');
		},
	});
}

// Get the sensors from the group id
// Param : group id (comes from the hidden input "groupId"
// Return : generate an accordion with the sensors' data
function getSensorsFromId() {
	$
			.ajax({
				type : 'GET',
				url : PROJECT_NAME + '/rest/sensors/' + $('#groupId').val(),
				headers : {
					'X-Token' : $.cookie('EnoToken')
				},
				statusCode : {
					401 : function() {
						window.location = "index.html";
					},
					500 : function() {
						window.location = "index.html";
					},
				},
				success : function(data, status, response) {
					var isEmpty = isEmptyObject(data); // check the object

					if (isEmpty == true) {
						// remove the content of the DIVs
						$('#sensors').html('');
						$('#groupLegend').html('');
						showAlert('#alertNoSensorError');
					} else {
						// create the title "Sensor(s) of <group's name>"
						var outputLegend = '<fieldset><legend>Sensor(s) of '
								+ $('#groups').children(":selected").val()
								+ '</legend>';
						$('#groupLegend').html(outputLegend);

						var output = '';
						// var measureTypesChecked = new Array(); // will
						// contain the eep shortcut(s) of the checked checkboxes
						var noAccordion = $('#noAccordion').val();
						output += '<div class="accordion" id="accordion'
								+ noAccordion + '">';

						$
								.each(
										data,
										function(key, value) {
											var lastModification = value.lastModification; // store
											// the
											// last
											// modification's
											// date
											// for
											// spliting
											// operation
											var actuator = '';
											if (value.actuator == true) {
												actuator += '( actuator )';
											}
											; // if the sensor is an
											// acttuator, we add
											// "(actuator)" on the sensor's
											// title

											// accordion head
											output += '<div class="accordion-group">';
											output += '<div class="accordion-heading">';
											output += '<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion'
													+ noAccordion
													+ '" href="#collapse'
													+ value.id
													+ '">'
													+ value.name
													+ ' '
													+ actuator + '</a>';
											output += '</div>';

											// accordion body
											output += '<div id="collapse'
													+ value.id
													+ '" class="accordion-body collapse">';
											output += '<div class="accordion-inner">';
											output += '<div class="row-fluid">';
											output += '<div class="span6">';

											// start form
											// column 1
											output += '<label for="sensorName'
													+ value.id
													+ '">Name :</label>';
											output += '<input type="text" id="sensorName'
													+ value.id
													+ '" class="span12" placeholder="Name ..." disabled value="'
													+ value.name + '">';
											output += '<label for="sensorDescription'
													+ value.id
													+ '">Description :</label>';
											output += '<input type="text" id="sensorDescription'
													+ value.id
													+ '" class="span12" placeholder="Description ..." disabled value="'
													+ value.description + '">';
											output += '<label for="sensorManufacturer'
													+ value.id
													+ '">Manufactur  er :</label>';
											output += '<select id="sensorManufacturer'
													+ value.id
													+ '" class="span12" disabled><option selected>'
													+ value.manufacturer
													+ '</option></select>';
											output += '<label for="sensorLastModification'
													+ value.id
													+ '">Last modification :</label>';
											output += '<input type="text" id="sensorLastModification'
													+ value.id
													+ '" class="span12" placeholder="Last modification ... by ..." disabled value="'
													+ lastModification.slice(8,
															10)
													+ '.'
													+ lastModification.slice(5,
															7)
													+ '.'
													+ lastModification.slice(0,
															4)
													+ ' by '
													+ value.lastModifier + '">';
											output += '<label for="sensorPath'
													+ value.id
													+ '">Location :</label>';
											output += '<div class="row-fluid">';
											output += '<div class="span10">';
											output += '<input type="text" id="sensorPath'
													+ value.id
													+ '" class="span12" placeholder="Location ..." disabled value="';
											if (value.locationPath) {
												output += value.locationPath
														+ '.';
											} else {
												output += value.locationName
														+ '.';
											}
											output += $('#dns').val()
													+ '" onClick="manageLocation();">';
											output += '</div>';
											output += '<div class="span2 text-right">';
											output += '<label><i class="'
													+ value.locationTypeImgUrl
													+ '"></i></label>';
											output += '</div>';
											// hidden input to store the
											// location id
											output += '<input type="hidden" id="sensorLocationId'
													+ value.id
													+ '" value="'
													+ value.locationId + '">';
											output += '</div>';
											output += '<label>Types of measurements :</label>';
											output += '<span id="measureTypeCheckboxes'
													+ value.id + '">';
											if (value.measure == "") {
												output += '-';
											} else {
												$
														.each(
																value['measure'],
																function(key,
																		measure) {
																	output += '<label class="checkbox"><input name="measureTypeCheckbox'
																			+ value.id
																			+ '" type="checkbox" value="'
																			+ measure.eepShortcut
																			+ '" checked disabled /> '
																			+ measure.eepShortcut
																			+ ' ';
																	if (measure.unit != null) {
																		output += '['
																				+ measure.unit
																				+ '] ';
																	}
																	output += '</label>';
																});
											}
											output += '</span>';
											output += '</div>';

											// column 2
											output += '<div class="span6">';
											output += '<label for="sensorEepRorg'
													+ value.id
													+ '">EEP rorg :</label>';
											// hidden input to store temporarily
											// the EEP rorg
											output += '<input type="hidden" id="tempEepRorg'
													+ value.id
													+ '" value="'
													+ value.eepRorg
															.toString(16)
															.toUpperCase()
													+ '">';
											output += '<select id="sensorEepRorg'
													+ value.id
													+ '" class="span12" disabled onchange="getEepFunctions();"><option selected>'
													+ value.eepRorg
															.toString(16)
															.toUpperCase()
													+ '</option></select>';
											output += '<label for="sensorEepFunction'
													+ value.id
													+ '">EEP function :</label>';
											// hidden input to store temporarily
											// the EEP function
											output += '<input type="hidden" id="tempEepFunction'
													+ value.id
													+ '" value="'
													+ value.eepFunction
															.toString(16)
															.toUpperCase()
													+ '">';
											output += '<select id="sensorEepFunction'
													+ value.id
													+ '" class="span12" disabled onchange="getEepTypes();"><option selected>'
													+ value.eepFunction
															.toString(16)
															.toUpperCase()
													+ '</option></select>';
											output += '<label for="sensorEepType'
													+ value.id
													+ '">EEP type :</label>';
											// hidden input to store temporarily
											// the EEP type
											output += '<input type="hidden" id="tempEepType'
													+ value.id
													+ '" value="'
													+ value.eepType
															.toString(16)
															.toUpperCase()
													+ '">';
											output += '<select id="sensorEepType'
													+ value.id
													+ '" class="span12" disabled onchange="getMeasureTypes();"><option selected>'
													+ value.eepType
															.toString(16)
															.toUpperCase()
													+ '</option></select>';
											output += '<label for="sensorAddress'
													+ value.id
													+ '">Address :</label>';
											output += '<input type="text" id="sensorAddress'
													+ value.id
													+ '" class="span12" placeholder="Address ..." disabled value="'
													+ value.address + '">';
											output += '<div class="row-fluid">';
											output += '<div class="span6">';
											output += '<label>Hybrid mode :</label>';

											if (value.hybridMode == true) {
												output += '<label class="radio inline"><input type="radio" id="sensorHybridTrue'
														+ value.id
														+ '" name="sensorHybridRadio'
														+ value.id
														+ '" value="true" checked disabled> Yes </label>';
												output += '<label class="radio inline"><input type="radio"id="sensorHybridFalse'
														+ value.id
														+ '" name="sensorHybridRadio'
														+ value.id
														+ '" value="false" disabled> No </label>';
											} else {
												output += '<label class="radio inline"><input type="radio" id="sensorHybridTrue'
														+ value.id
														+ '" name="sensorHybridRadio'
														+ value.id
														+ '" value="true" disabled> Yes </label>';
												output += '<label class="radio inline"><input type="radio"id="sensorHybridFalse'
														+ value.id
														+ '" name="sensorHybridRadio'
														+ value.id
														+ '" value="false" checked disabled> No </label>';
											}
											output += '</div>';
											output += '<div class="span6">';
											output += '<label>Actuator :</label>';
											if (value.actuator == true) {
												output += '<label class="radio inline"><input type="radio" id="sensorActuatorTrue'
														+ value.id
														+ '" name="sensorActuatorRadio'
														+ value.id
														+ '" value="true" checked disabled> Yes </label>';
												output += '<label class="radio inline"><input type="radio"id="sensorActuatorFalse'
														+ value.id
														+ '" name="sensorActuatorRadio'
														+ value.id
														+ '" value="false" disabled> No </label>';
											} else {
												output += '<label class="radio inline"><input type="radio" id="sensorActuatorTrue'
														+ value.id
														+ '" name="sensorActuatorRadio'
														+ value.id
														+ '" value="true" disabled> Yes </label>';
												output += '<label class="radio inline"><input type="radio"id="sensorActuatorFalse'
														+ value.id
														+ '" name="sensorActuatorRadio'
														+ value.id
														+ '" value="false" checked disabled> No </label>';
											}
											output += '</div>';
											output += '</div>';
											output += '</div>';
											output += '</div>';

											output += '<div class="row-fluid">';
											output += '<div id="sensorButtons'
													+ value.id
													+ '" class="span12 text-right">';
											output += '<div class="btn-group"><button type="button" class="btn" onClick="editSensor('
													+ value.id
													+ ');">Edit</button>';
											output += '<button type="button" class="btn btn-danger" onClick="deleteSensor('
													+ value.id
													+ ');">Delete</button>';
											output += '</div>';
											output += '</div>';
											output += '</div>';

											output += '</div>';
											output += '</div>';
											output += '</div>';
										});
						output += '</div>';
						noAccordion++;
						$('#noAccordion').val(noAccordion);
						$('#sensors').html(output);
					}
				},
				error : function(response, status, error) {
					showAlert('#alertGroupError');
				},
			});
}

// Enable the sensor's input and generate the manufacturers' list
function editSensor(id) {
	// get the current manufacturer
	var manufacturer = $('#sensorManufacturer' + id + '').children(":selected")
			.val();

	// generate the list of manufacturers
	$.ajax({
		type : 'GET',
		url : PROJECT_NAME + '/rest/manufacturers',
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			var output = "";
			$.each(data, function(key, value) {
				if (value.name == manufacturer) {
					// set the attribut "selected" to the current manufacturer
					output += '<option value="' + value.name + '" selected>'
							+ value.name + '</option>';
				} else {
					output += '<option value="' + value.name + '">'
							+ value.name + '</option>';
				}
			});
			$('#sensorManufacturer' + id + '').html(output);
		},
		error : function(response, status, error) {
		},
	});

	// enable the sensor's inputs
	$('#sensorName' + id + '').prop('disabled', false);
	$('#sensorDescription' + id + '').prop('disabled', false);
	$('#sensorManufacturer' + id + '').prop('disabled', false);
	$('#sensorEepRorg' + id + '').prop('disabled', false);
	$('#sensorEepFunction' + id + '').prop('disabled', false);
	$('#sensorEepType' + id + '').prop('disabled', false);
	$('#sensorAddress' + id + '').prop('disabled', false);
	$('#sensorPath' + id + '').prop('disabled', false);
	$('#sensorHybridTrue' + id + '').prop('disabled', false);
	$('#sensorHybridFalse' + id + '').prop('disabled', false);
	$('#sensorActuatorTrue' + id + '').prop('disabled', false);
	$('#sensorActuatorFalse' + id + '').prop('disabled', false);

	$("input:checkbox[name=measureTypeCheckbox" + id + "]:checked").each(
			function() {
				$(this).prop('disabled', false);
			});

	$('#sensorId').val(id); // set the hidden input with the sensor id (will be
	// used if we change his location)

	// add the button "Cancel" and "Save changes"
	var output = '';
	output += '<div class="btn-group">';
	output += '<button type="button" class="btn" onClick="cancelSensor(' + id
			+ ');">Cancel</button>';
	output += '<button type="button" class="btn btn-success" onClick="updateSensor('
			+ id + ');">Save changes</button>';
	output += '</div>';
	$('#sensorButtons' + id + '').html(output);

	// load the eeps with the description
	var eepRorg = $('#sensorEepRorg' + $('#sensorId').val() + '').val();

	$.ajax({
		type : 'GET',
		url : PROJECT_NAME + '/rest/eep_rorgs',
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			var output = '';
			$.each(data, function(key, value) {
				var eepRorgCut = value.eepRorg.substring(2, 4);
				if (eepRorg == eepRorgCut) {
					output += '<option id="' + value.eepRorg + '" selected>'
							+ value.eepRorg + ' - ' + value.description
							+ '</option>';
				} else {
					output += '<option id="' + value.eepRorg + '">'
							+ value.eepRorg + ' - ' + value.description
							+ '</option>';
				}
			});

			$('#sensorEepRorg' + $('#sensorId').val() + '').html(output);
		},
		error : function(response, status, error) {
		},
	});

	var eepFunction = $('#sensorEepFunction' + $('#sensorId').val() + '').val();
	if (eepFunction.length == 1)
		eepFunction = '0' + eepFunction;

	$.ajax({
		type : 'GET',
		url : PROJECT_NAME + '/rest/eep_functions/0x' + eepRorg,
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			var output = '';
			$.each(data, function(key, value) {
				var eepFunctionCut = value.eepFunction.substring(2, 4);
				if (eepFunction == eepFunctionCut) {
					output += '<option id="' + value.eepFunction
							+ '" selected>' + value.eepFunction + ' - '
							+ value.description + '</option>';
				} else {
					output += '<option id="' + value.eepFunction + '">'
							+ value.eepFunction + ' - ' + value.description
							+ '</option>';
				}
			});

			$('#sensorEepFunction' + $('#sensorId').val() + '').html(output);
		},
		error : function(response, status, error) {
			console.log('Error : ' + error);
		},
	});

	var eepType = $('#sensorEepType' + $('#sensorId').val() + '').val();
	if (eepType.length == 1)
		eepType = '0' + eepType;

	$.ajax({
		type : 'GET',
		url : PROJECT_NAME + '/rest/eep_types/0x' + eepRorg + '/0x'
				+ eepFunction,
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			var output = '';
			$.each(data, function(key, value) {
				var eepTypeCut = value.eepType.substring(2, 4);
				if (eepType == eepTypeCut) {
					output += '<option id="' + value.eepType + '" selected>'
							+ value.eepType + ' - ' + value.description
							+ '</option>';
				} else {
					output += '<option id="' + value.eepType + '">'
							+ value.eepType + ' - ' + value.description
							+ '</option>';
				}
			});

			$('#sensorEepType' + $('#sensorId').val() + '').html(output);
		},
		error : function(response, status, error) {
		},
	});
}

// Disable the sensor's buttons
function cancelSensor(id) {
	// returne the initial value of the eep
	$('#sensorEepRorg' + id + '').html(
			'<option selected>' + $('#tempEepRorg' + id + '').val()
					+ '</option>');
	$('#sensorEepFunction' + id + '').html(
			'<option selected>' + $('#tempEepFunction' + id + '').val()
					+ '</option>');
	$('#sensorEepType' + id + '').html(
			'<option selected>' + $('#tempEepType' + id + '').val()
					+ '</option>');

	// disable the buttons
	$('#sensorName' + id + '').prop('disabled', true);
	$('#sensorDescription' + id + '').prop('disabled', true);
	$('#sensorManufacturer' + id + '').prop('disabled', true);
	$('#sensorEepRorg' + id + '').prop('disabled', true);
	$('#sensorEepFunction' + id + '').prop('disabled', true);
	$('#sensorEepType' + id + '').prop('disabled', true);
	$('#sensorAddress' + id + '').prop('disabled', true);
	$('#sensorPath' + id + '').prop('disabled', true);
	$('#sensorHybridTrue' + id + '').prop('disabled', true);
	$('#sensorHybridFalse' + id + '').prop('disabled', true);
	$('#sensorActuatorTrue' + id + '').prop('disabled', true);
	$('#sensorActuatorFalse' + id + '').prop('disabled', true);

	$("input:checkbox[name=measureTypeCheckbox" + id + "]:checked").each(
			function() {
				$(this).prop('disabled', true);
			});

	// add the buttons "Edit" and "Delete"
	var output = '';
	output += '<div class="btn-group">';
	output += '<button type="button" class="btn" onClick="editSensor(' + id
			+ ');">Edit</button>';
	output += '<button type="button" class="btn btn-danger" onClick="deleteSensor('
			+ id + ');">Delete</button>';
	output += '</div>';
	$('#sensorButtons' + id + '').html(output);
}

function createSensor() {
	// keep the value of al the checked checkboxes (for measure types)
	var selectedMeasures = new Array();
	$("input:checkbox[name=measureTypeCheckbox0]:checked").each(function() {
		selectedMeasures.push($(this).val());
	});
	var measuresString = selectedMeasures.join('/'); // create an unique
	// String with all the
	// checked checkboxes

	// create a "sensor" with the input's data
	var sensor = {
		measure : measuresString,
		actuator : $('input:radio[name=sensorActuatorRadio0]:checked').val(),
		address : $('#sensorAddress0').val(),
		description : $('#sensorDescription0').val(),
		eepRorg : $('#sensorEepRorg0').val(),
		eepFunction : $('#sensorEepFunction0').val(),
		eepType : $('#sensorEepType0').val(),
		hybridMode : $('input:radio[name=sensorHybridRadio0]:checked').val(),
		lastModifier : $.cookie('EnoUsr'),
		locationPath : $('#sensorPath0').val(),
		manufacturer : $('#sensorManufacturer0').children(":selected").val(),
		name : $('#sensorName0').val(),
		locationId : $('#sensorLocationId0').val(),
	};

	$.ajax({
		type : 'PUT',
		url : PROJECT_NAME + '/rest/sensor/' + $('#groupId').val(),
		contentType : 'application/json',
		data : JSON.stringify(sensor),
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		// statusCode: {
		// 401: function () {
		// window.location = "index.html";
		// },
		// 500: function () {
		// window.location = "index.html";
		// },
		// },
		success : function(data, status, response) {
			$('#addSensor').modal('hide');
			getSensorsFromId(); // refresh the list
			showAlert('#alertGroupSuccess');

			// erase the content of the inputs
			$('#sensorName0').val('');
			$('#sensorDescription0').val('');
			$('#sensorEepFunction0').val('');
			$('#sensorEepType0').val('');
			$('#sensorAddress0').val('');
			$('#sensorPath0').val('');
			$('#measureTypeCheckboxes0').html('-');
		},
		error : function(response, status, error) {
			console.log(error);
		},
	});
}

// Update the specified sensor
// Param : sensors' inputs (defiined by id)
function updateSensor(id) {
	// keep the value of al the checked checkboxes (for measure types)
	var selectedMeasures = new Array();
	$("input:checkbox[name=measureTypeCheckbox" + id + "]:checked").each(
			function() {
				selectedMeasures.push($(this).val());
			});
	var measuresString = selectedMeasures.join('/'); // create an unique
	// String with all the
	// checked checkboxes

	// create a "sensor" with the input's data
	var sensor = {
		measure : measuresString,
		actuator : $('input:radio[name=sensorActuatorRadio' + id + ']:checked')
				.val(),
		address : $('#sensorAddress' + id + '').val(),
		description : $('#sensorDescription' + id + '').val(),
		eepRorg : $('#sensorEepRorg' + id + '').val(),
		eepFunction : $('#sensorEepFunction' + id + '').val(),
		eepType : $('#sensorEepType' + id + '').val(),
		hybridMode : $('input:radio[name=sensorHybridRadio' + id + ']:checked')
				.val(),
		lastModifier : $.cookie('EnoUsr'),
		locationPath : $('#sensorPath' + id + '').val(),
		manufacturer : $('#sensorManufacturer' + id + '').children(":selected")
				.val(),
		name : $('#sensorName' + id + '').val(),
		locationId : $('#sensorLocationId' + id + '').val(),
	};

	$.ajax({
		type : 'POST',
		url : PROJECT_NAME + '/rest/sensor/' + id,
		contentType : 'application/json',
		data : JSON.stringify(sensor),
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		// statusCode: {
		// 401: function () {
		// window.location = "index.html";
		// },
		// 500: function () {
		// window.location = "index.html";
		// },
		// },
		success : function(data, status, response) {
			showAlert('#alertSensorSuccess');
			cancelSensor(id); // disable the inputs
		},
		error : function(response, status, error) {
			showAlert('#alertSensorError');
		},
	});
}

// Delete the specified sensor
// Param : sensor id
function deleteSensor(id) {
	$.ajax({
		type : 'DELETE',
		url : PROJECT_NAME + '/rest/sensor/' + $('#groupId').val() + '/' + id,
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			showAlert('#alertSensorSuccess');
			getSensorsFromId(); // reload the group's sensors
		},
		error : function(response, status, error) {
			showAlert('#alertSensorError');
		},
	});
}

//
// EEPS
//

function getEepRorgs(selectedRorg, selectedFunction, selectedType) {
	$.ajax({
		type : 'GET',
		url : PROJECT_NAME + '/rest/eep_rorgs',
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			var output = '';
			var found = false;
			$.each(data, function(key, value) {
				if (value.eepRorg == selectedRorg) {
					output += '<option id="' + value.eepRorg + '" selected>'
							+ value.eepRorg + ' - ' + value.description
							+ '</option>';
					found = true;
				} else {
					output += '<option id="' + value.eepRorg + '">'
							+ value.eepRorg + ' - ' + value.description
							+ '</option>';
				}
			});

			$('#sensorEepRorg' + $('#sensorId').val() + '').html(output);
			if (found)
				getEepFunctions(selectedFunction, selectedType);
		},
		error : function(response, status, error) {
		},
	});
}

function getEepFunctions(selectedFunction, selectedType) {
	$.ajax({
		type : 'GET',
		url : PROJECT_NAME
				+ '/rest/eep_functions/'
				+ $('#sensorEepRorg' + $('#sensorId').val() + '').children(
						":selected").attr("id"),
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			var output = '';
			var count = 0;
			var found = false;
			$.each(data, function(key, value) {
				count++;
				if (value.eepFunction == selectedFunction) {
					output += '<option id="' + value.eepFunction
							+ '" selected>' + value.eepFunction + ' - '
							+ value.description + '</option>';
					found = true;
				} else {
					output += '<option id="' + value.eepFunction + '">'
							+ value.eepFunction + ' - ' + value.description
							+ '</option>';
				}
			});

			$('#sensorEepFunction' + $('#sensorId').val() + '').html(output);
			if (count == 1 || found)
				getEepTypes(selectedType);
		},
		error : function(response, status, error) {
		},
	});
}

function getEepTypes(selectedType) {
	$.ajax({
		type : 'GET',
		url : PROJECT_NAME
				+ '/rest/eep_types/'
				+ $('#sensorEepRorg' + $('#sensorId').val() + '').children(
						":selected").attr("id")
				+ '/'
				+ $('#sensorEepFunction' + $('#sensorId').val() + '').children(
						":selected").attr("id"),
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			var output = '';
			var count = 0;
			var found = false;
			$.each(data, function(key, value) {
				if (value.eepType == selectedType) {
					output += '<option id="' + value.eepType + '" selected>'
							+ value.eepType + ' - ' + value.description
							+ '</option>';
					found = true;
				} else {
					output += '<option id="' + value.eepType + '">'
							+ value.eepType + ' - ' + value.description
							+ '</option>';
				}
				count++;
			});

			$('#sensorEepType' + $('#sensorId').val() + '').html(output);
			if (count == 1 || found)
				getMeasureTypes();
		},
		error : function(response, status, error) {
		},
	});
}

// Show a pop-up with the measure types available for the EEPs
function getMeasureTypes() {
	$('#measureTypesAvailable').html('');
	$
			.ajax({
				type : 'GET',
				url : PROJECT_NAME
						+ '/rest/measure_types/'
						+ $('#sensorEepRorg' + $('#sensorId').val() + '')
								.children(":selected").attr("id")
						+ '/'
						+ $('#sensorEepFunction' + $('#sensorId').val() + '')
								.children(":selected").attr("id")
						+ '/'
						+ $('#sensorEepType' + $('#sensorId').val() + '')
								.children(":selected").attr("id"),
				headers : {
					'X-Token' : $.cookie('EnoToken')
				},
				success : function(data, status, response) {
					var output = '';
					$
							.each(
									data,
									function(key, value) {
										// output += '<h3>' + value.shortcut + '
										// [' + value.unit + ']</h3>';
										output += '<label class="checkbox"><input name="measureTypeCheckbox" type="checkbox" value="'
												+ value.shortcut
												+ '" checked /> '
												+ value.shortcut + ' ';
										if (value.unit != null) {
											output += '[' + value.unit + '] ';
										}
										output += '</label>';
									});
					$('#measureTypesAvailable').html(output);
				},
				error : function(response, status, error) {
					console.log('Eerror : ' + error);
				},
			});
	if ($('#sensorId').val() == 0 || $('#sensorId').val() == -1) {
		$('#addSensor').modal('hide');
	}
	$('#manageMeasure').modal('show'); // show the pop-up
}

function saveMeasureTypes() {
	// keep the value of al the checked checkboxes (for measure types)
	var selectedMeasures = new Array();
	$("input:checkbox[name=measureTypeCheckbox]:checked").each(function() {
		selectedMeasures.push($(this).val());
	});

	var output = "";
	for ( var i = 0; i < selectedMeasures.length; i++) {
		output += '<label class="checkbox"><input name="measureTypeCheckbox';
		output += $('#sensorId').val();
		output += '" type="checkbox" value="' + selectedMeasures[i]
				+ '" checked /> ' + selectedMeasures[i] + '</label>';
	}
	$('#measureTypeCheckboxes' + $('#sensorId').val() + '').html(output);

	if ($('#sensorId').val() == 0 || $('#sensorId').val() == -1) {
		$('#manageMeasure').modal('hide');
		$('#addSensor').modal('show');
	}
}

//
// LOCATIONS
//

// Show the "Manage location" pop-up and return the root locations
function manageLocation() {
	// get the locations (root)
	$
			.ajax({
				type : 'GET',
				url : PROJECT_NAME + '/rest/locations',
				headers : {
					'X-Token' : $.cookie('EnoToken')
				},
				statusCode : {
					401 : function() {
						window.location = "index.html";
					},
					500 : function() {
						window.location = "index.html";
					},
				},
				success : function(data, status, response) {
					var isEmpty = isEmptyObject(data); // check the content of
					// the object

					if (isEmpty == false) {
						var output = "";
						var noAccordion = $('#noAccordion').val();

						output += '<div class="accordion" id="accordion'
								+ noAccordion + '">';
						$
								.each(
										data,
										function(key, value) {
											var noCollapse = $('#noCollapse')
													.val(); // get the
											// noCollapse value
											// (for accordion
											// construction)

											output += '<div class="accordion-group">';

											// accordion header
											output += '<div class="accordion-heading">';
											// location's titel : icon (location
											// type) + location's name
											output += '<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion'
													+ noAccordion
													+ '" href="#collapse'
													+ noCollapse
													+ '" onClick="getLocationChilds('
													+ value.id
													+ '); selectLocation('
													+ value.id
													+ ', \''
													+ value.name
													+ '\')"><i class="'
													+ value.typeImgUrl
													+ '"></i> '
													+ value.name
													+ '</a>';
											output += '</div>';

											// accordion body
											output += '<div id="collapse'
													+ noCollapse
													+ '" class="accordion-body collapse">';
											noCollapse++; // ++ and save
											$('#noCollapse').val(noCollapse);
											output += '<div class="accordion-inner">';
											// store the location's path into
											// the hidden input
											output += '<input type="hidden" id="locationPath'
													+ value.id
													+ '" value="'
													+ value.path + '">';
											output += '<div class="row-fluid">';
											output += '<div id="locationChilds'
													+ value.id
													+ '" class="span12">';
											// here will come the child
											// locations
											output += '</div>';
											output += '</div>';
											output += '</div>';
											output += '</div>';
											output += '</div>';
										});
						output += '</div>';
						noAccordion++;
						$('#noAccordion').val(noAccordion)
						$('#locations').html(output);

						var buttons = '';
						buttons += '<button type="button" class="btn" onClick="addRootLocation();">Add root location</button>';
						$('#locationButtons').html(buttons);
					}
				},
				error : function(response, status, error) {
					showAlert('#alertGroupError');
				},
			});

	$('#editLocation').html(''); // erase the content of the DIV
	if ($('#sensorId').val() == 0 || $('#sensorId').val() == -1) {
		$('#addSensor').modal('hide');
	}
	$('#manageLocation').modal('show'); // show the pop-up
}

// Load the child locations
// Param : parent location id (comes from his location's parent <a>)
function getLocationChilds(id) {
	$
			.ajax({
				type : 'GET',
				url : PROJECT_NAME + '/rest/locations/' + id,
				headers : {
					'X-Token' : $.cookie('EnoToken')
				},
				statusCode : {
					401 : function() {
						window.location = "index.html";
					},
					500 : function() {
						window.location = "index.html";
					},
				},
				success : function(data, status, response) {
					var isEmpty = isEmptyObject(data); // check the content of
					// the object

					if (isEmpty == true) {
						// if there isn't any child location, we create an alert
						var output = '';
						output += '<div class="alert alert-info">';
						output += '<button type="button" class="close" onClick="$(this).parent().hide();">&times;</button>';
						output += '<strong>Heads up!</strong> There isn\'t child locations.';
						output += '</div>';
						$('#locationChilds' + id + '').html(output);
					} else {
						var output = "";
						// get the noCollapse & noAccordion values
						var noCollapse = $('#noCollapse').val();
						var noAccordion = $('#noAccordion').val();

						output += '<div class="accordion" id="accordion'
								+ noAccordion + '">';
						$
								.each(
										data,
										function(key, value) {
											output += '<div class="accordion-group">';
											// accordion header
											output += '<div class="accordion-heading">';
											// location's titel : icon (location
											// type) + location's name
											output += '<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion'
													+ noAccordion
													+ '" href="#collapse'
													+ noCollapse
													+ '" onClick="getLocationChilds('
													+ value.id
													+ '); selectLocation('
													+ value.id
													+ ', \''
													+ value.name
													+ '\')"><i class="'
													+ value.typeImgUrl
													+ '"></i> '
													+ value.name
													+ '</a>';
											output += '</div>';
											// accordion body
											output += '<div id="collapse'
													+ noCollapse
													+ '" class="accordion-body collapse">';
											noCollapse++;
											output += '<div class="accordion-inner">';
											// store the location's path into
											// the hidden input
											output += '<input type="hidden" id="locationPath'
													+ value.id
													+ '" value="'
													+ value.path + '">';
											output += '<div class="row-fluid">';
											output += '<div id="locationChilds'
													+ value.id
													+ '" class="span12">';
											// here will come the child
											// locations
											output += '</div>';
											output += '</div>';
											output += '</div>';
											output += '</div>';
											output += '</div>';
										});
						output += '</div>';
						noAccordion++;
						$('#noCollapse').val(noCollapse);
						$('#noAccordion').val(noAccordion)
						$('#locationChilds' + id + '').html(output);
					}
				},
				error : function(response, status, error) {
					showAlert('#alertGroupError');
				},
			});
}

// Update the hidden inputs "locationSelectedPath"
// Param : location id (comes from getLocationsChilds(id))
function selectLocation(id, name) {
	$('#locationId').val(id); // store the selected location id into the
	// hidden input
	if ($('#locationPath' + id + '').val() != 'null')
		$('#locationSelectedPath').val(
				name + '.' + $('#locationPath' + id + '').val() + '.'
						+ $('#dns').val()); // update the selected path (pop-up
	// "Manage location")
	else
		$('#locationSelectedPath').val(name + '.' + $('#dns').val()); // update
	// the
	// selected
	// path
	// (pop-up
	// "Manage
	// location")

	// add the buttons "Add location", *Edit" & "Delete"
	var output = '';
	output += '<div class="btn-group">';
	output += '<button type="button" class="btn" onClick="addLocation(' + id
			+ ')";">Add location</button>';
	output += '<button type="button" class="btn btn" onClick="editLocation('
			+ id + ');">Edit</button>';
	output += '<button type="button" class="btn btn-danger" onClick="deleteLocation('
			+ id + ');">Delete</button>';
	output += '</div>';
	$('#locationButtons').html(output);

	$('#editLocation').html(''); // erase the content of the DIV
}

// Update the location id and the location path (onClick "Save* on "Manage
// location" pop-up)
function saveLocation() {
	// get the hidden inputs' values
	var sensorId = $('#sensorId').val();
	var locationId = $('#locationId').val();
	var path = $('#locationSelectedPath').val();

	$('#sensorLocationId' + sensorId + '').val(locationId); // update the
	// location id of
	// the sensor
	$('#sensorPath' + sensorId + '').val(path); // update the location's path

	if ($('#sensorId').val() == 0 || $('#sensorId').val() == -1) {
		$('#manageLocation').modal('hide');
		$('#addSensor').modal('show');
	}
}

// Generate a form for creating a new root location
function addRootLocation() {
	var output = '';
	output += '<div class="span6">';
	output += '<label for="locationName">Name :</label>';
	output += '<input type="text" id="locationName" class="span12" placeholder="Name ...">';
	output += '</div>';
	output += '<div class="span6">';
	output += '<label for="locationType">Type :</label>';
	output += '<select id="locationType" class="span12" size="1">';
	output += '</select>';
	output += '</div>';
	$('#editLocation').html(output);

	// get the location's lypes
	$.ajax({
		type : 'GET',
		url : PROJECT_NAME + '/rest/locationtypes',
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			var output = '';
			$.each(data, function(key, value) {
				output += '<option value="' + value.locationType + '">'
						+ value.locationType + '</option>';
			});

			$('#locationType').html(output);
		},
		error : function(response, status, error) {
		},
	});

	// add the buttons : Cancel or Save
	var output = '';
	output += '<div class="btn-group">';
	output += '<button type="button" class="btn" onClick="cancelLocation();">Cancel</button>';
	output += '<button type="button" class="btn btn-primary" onClick="createRootLocation();">Save</button>';
	output += '</div>';
	$('#locationButtons').html(output);
}

// Hide the form for creating a location
function cancelLocation() {
	$('#editLocation').html(''); // erase the DIV's content

	// create the button "Add root location"
	var output = '';
	output += '<button type="button" class="btn" onClick="addRootLocation();">Add root location</button>';
	$('#locationButtons').html(output);
}

// Create a new root location
// Param : name & typeName (from location's inputs)
function createRootLocation() {
	// create a "location"
	var location = {
		name : $('#locationName').val(),
		typeName : $('#locationType').children(":selected").val()
	};

	$.ajax({
		type : 'PUT',
		url : PROJECT_NAME + '/rest/location',
		contentType : 'application/json',
		data : JSON.stringify(location),
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			cancelLocation(); // hide the form
			manageLocation(); // refresh the list
		},
		error : function(response, status, error) {
		},
	});
}

// Create a new location
// Param : name & typeName (from location's inputs)
function createLocation(id) {
	// create a location
	var location = {
		name : $('#locationName').val(),
		typeName : $('#locationType').children(":selected").val()
	};

	$.ajax({
		type : 'PUT',
		url : PROJECT_NAME + '/rest/location/' + id,
		contentType : 'application/json',
		data : JSON.stringify(location),
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			cancelLocation(); // hide the form
			manageLocation(); // refresh the list
		},
		error : function(response, status, error) {
		},
	});
}

// Generate a form for creating a new location
// Param : location parent id
function addLocation(id) {
	var output = '';
	output += '<div class="span6">';
	output += '<label for="locationName">Name :</label>';
	output += '<input type="text" id="locationName" class="span12" placeholder="Name ...">';
	output += '</div>';
	output += '<div class="span6">';
	output += '<label for="locationType">Type :</label>';
	output += '<select id="locationType" class="span12" size="1">';
	output += '</select>';
	output += '</div>';
	$('#editLocation').html(output);

	// get the location's lypes
	$.ajax({
		type : 'GET',
		url : PROJECT_NAME + '/rest/locationtypes',
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			var output = '';
			$.each(data, function(key, value) {
				output += '<option value="' + value.locationType + '">'
						+ value.locationType + '</option>';
			});

			$('#locationType').html(output);
		},
		error : function(response, status, error) {
		},
	});

	// add the buttons : Cancel or Save with relative parent location id
	var buttons = '';
	buttons += '<div class="btn-group">';
	buttons += '<button type="button" class="btn" onClick="cancelLocation();">Cancel</button>';
	buttons += '<button type="button" class="btn btn-primary" onClick="createLocation('
			+ id + ');">Save</button>';
	buttons += '</div>';
	$('#locationButtons').html(buttons);
}

// Show the location form and load the actual data of the location
// Param : location id
function editLocation(id) {
	var output = '';
	output += '<div class="span6">';
	output += '<label for="locationName">Name :</label>';
	output += '<input type="text" id="locationName" class="span12" placeholder="Name ...">';
	output += '</div>';
	output += '<div class="span6">';
	output += '<label for="locationType">Type :</label>';
	output += '<select id="locationType" class="span12" size="1">';
	output += '</select>';
	output += '</div>';
	$('#editLocation').html(output);

	var locationTypeName = ''; // will store the current location type
	var options = '';

	// get the location's info
	$.ajax({
		type : 'GET',
		url : PROJECT_NAME + '/rest/location/' + id,
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			// get the value and set the inputs
			$('#locationName').val(data.name);
			locationTypeName = data.typeName;
		},
		error : function(response, status, error) {
		},
	});

	// get the location's lypes
	$.ajax({
		type : 'GET',
		url : PROJECT_NAME + '/rest/locationtypes',
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			var output = '';
			$.each(data, function(key, value) {
				if (value.locationType == locationTypeName) {
					// set the attribut "selected" to the current manufacturer
					options += '<option value="' + value.locationType
							+ '"selected>' + value.locationType + '</option>';
				} else {
					options += '<option value="' + value.locationType + '">'
							+ value.locationType + '</option>';
				}
			});

			$('#locationType').html(options);
		},
		error : function(response, status, error) {
		},
	});

	// add the buttons : Cancel or Save with relative parent location id
	var buttons = '';
	buttons += '<div class="btn-group">';
	buttons += '<button type="button" class="btn" onClick="cancelLocation();">Cancel</button>';
	buttons += '<button type="button" class="btn btn-primary" onClick="updateLocation('
			+ id + ');">Save</button>';
	buttons += '</div>';
	$('#locationButtons').html(buttons);
}

// Update the specified location
// Param : location id
function updateLocation(id) {
	// create a location
	var location = {
		name : $('#locationName').val(),
		typeName : $('#locationType').children(":selected").val()
	};

	$.ajax({
		type : 'POST',
		url : PROJECT_NAME + '/rest/location/' + id,
		contentType : 'application/json',
		data : JSON.stringify(location),
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			cancelLocation(); // hide the form
			manageLocation(); // refresh the list
		},
		error : function(response, status, error) {
		},
	});
}

// Delete the specified location
// Param : location id
function deleteLocation(id) {
	$.ajax({
		type : 'DELETE',
		url : PROJECT_NAME + '/rest/location/' + id,
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			manageLocation(); // refresh the locations' list
		},
		error : function(response, status, error) {
		},
	});
}

//
// ADD OR CREATE SENSOR
// 

// Search an existing sensor with the given text (from the input)
function searchSensor() {
	// get the value of the input
	var searchField = $('#searchInput').val(); // get the input value
	var myExp = new RegExp(searchField, "i"); // create an expression
	var noResults = true; // if false, we show the resluts / if true, we show
	// an alert

	$
			.ajax({
				type : 'GET',
				url : PROJECT_NAME + '/rest/sensors',
				headers : {
					'X-Token' : $.cookie('EnoToken')
				},
				// statusCode: {
				// 401: function () {
				// window.location = "index.html";
				// },
				// 500: function () {
				// window.location = "index.html";
				// },
				// },
				success : function(data, status, response) {
					var output = '';
					var results = 0;
					// search info
					output += '<label>Found sensor(s) for "' + searchField
							+ '* :</label';
					// the found sensors
					output += '<div class="accordion" id="accordionSearch">';
					searchField = searchField.toLowerCase();
					console.log("Search :" + searchField);
					$
							.each(
									data,
									function(key, value) {
										// if the results correspond to the
										// expression, we show them
										if (value.name.toLowerCase().search(
												searchField) != -1) {
											results++;
											console.log("Result : "
													+ value.name);

											output += '<div class="accordion-group">';
											output += '<div class="accordion-heading">';
											output += '<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordionSearch" href="#existingCollapse'
													+ value.id
													+ '">'
													+ value.name + '</a>';
											output += '</div>';
											output += '<div id="existingCollapse'
													+ value.id
													+ '" class="accordion-body collapse">';
											output += '<div class="accordion-inner">';
											output += '<div class="row-fluid">';
											// column 1
											output += '<div class="span6">';
											output += '<label for="existingSnsorName'
													+ value.id
													+ '">Name :</label>';
											output += '<input type="text" id="existingSensorName'
													+ value.id
													+ '" class="span12" placeholder="Name ..." disabled value="'
													+ value.name + '">';
											output += '<label for="existingSensorDescription'
													+ value.id
													+ '">Description :</label>';
											output += '<input type="text" id="existingSensorDescription'
													+ value.id
													+ '" class="span12" placeholder="Description ..." disabled value="'
													+ value.description + '">';
											output += '</div>';
											// column 2
											output += '<div class="span6">';
											output += '<label for="existingSensorLocation'
													+ value.id
													+ '">Location :</label>';
											output += '<input type="text" id="existingSensorLocation'
													+ value.id
													+ '" class="span12" placeholder="Location ..." disabled value="'
													+ value.locationPath
													+ ' ( '
													+ value.locationTypeName
													+ ' )">';
											output += '</div>';
											output += '</div>';

											output += '<div class="row-fluid">';
											output += '<div class="span12 text-right">';
											output += '<div class="btn-group">';
											output += '<button type="button" class="btn btn-primary" onClick="addExistingSensor('
													+ value.id
													+ ');">Add</button>';
											output += '</div>';
											output += '</div>';
											output += '</div>';
											output += '</div>';
											output += '</div>';
											output += '</div>';
										}
									});
					if (results == 0) {
						output = '<div id="alertNoGroupError" class="alert alert-info"><button type="button" class="close" onClick="$(this).parent().hide();">&times;</button><strong>Heads up!</strong> There are no results for your search.</div>';
					} else {
						output += '</div>';
					}
					$('#searchSensor').html(output);
				},
				error : function(response, status, error) {
					console.log('Error : ' + error);
				},
			});
}

// Add an existing sensor to a specified group
// Param : sensor id + group id (comes from input "groupId")
function addExistingSensor(id) {
	$('#addSensor').modal('hide'); // hide the "add sensor" pop-up

	$.ajax({
		type : 'PUT',
		url : PROJECT_NAME + '/rest/sensor/' + $('#groupId').val() + '/' + id,
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			$('#addSensor').modal('hide');
			getSensorsFromId(); // refresh the list
			showAlert('#alertGroupSuccess');

			// erase the actual search
			$('#searchSensor').html('');
			$('#searchInput').val('');
		},
		error : function(response, status, error) {
			showAlert('#alertGroupError');
		},
	});
}

function getSensorsInLearnMode() {
	$
			.ajax({
				type : 'GET',
				url : PROJECT_NAME + '/rest/sensors_learn',
				headers : {
					'X-Token' : $.cookie('EnoToken')
				},
				statusCode : {
					401 : function() {
						window.location = "index.html";
					},
					500 : function() {
						window.location = "index.html";
					},
				},
				success : function(data, status, response) {
					var output = '';
					var count = 0;
					$.each(data,
							function(key, value) {
								output += '<option value="' + value.EEPRorg
										+ '/' + value.EEPFunction + '/'
										+ value.EEPType + '/' + value.address
										+ '/' + value.manufacturer
										+ '">EEP Rorg : ' + value.EEPRorg
										+ ' - EEP Function : '
										+ value.EEPFunction + ' - EEP Type : '
										+ value.EEPType + ' - Manufacturer : '
										+ value.manufacturer + ' - Address : '
										+ value.address + '</option>';
								count++;
							});
					if (output == '') {
						output += '<option disabled>There is no recently discovered devices</option>';
					}
					$('#sensorsInLearnMode').html(output);
					if (count == 1)
						updateLearnSensorInfo();
				},
				error : function(response, status, error) {
					console.log("Error : " + error);
				},
			});
}

function updateLearnSensorInfo() {
	var sensorInfo = $('#sensorsInLearnMode').val().split("/");

	// generate the list of manufacturers
	$.ajax({
		type : 'GET',
		url : PROJECT_NAME + '/rest/manufacturers',
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			var manufacturer = "";
			$.each(data, function(key, value) {
				if (value.id == sensorInfo[4]) {
					// set the attribut "selected" to the current manufacturer
					manufacturer += '<option id"' + value.name + '" selected>'
							+ value.name + '</option>';
				} else {
					manufacturer += '<option id="' + value.name + '">'
							+ value.name + '</option>';
				}
			});
			$('#sensorManufacturer-1').html(manufacturer);
		},
		error : function(response, status, error) {
		},
	});

	$('#sensorAddress-1').val(parseInt(sensorInfo[3], 16)); // set the sensor
	// address

	var eRorg = 0;
	var eFunction = 0;
	var eType = 0;

	// load the eeps with the description
	var eepRorg = parseInt(sensorInfo[0], 10).toString(16).toUpperCase();
	if (eepRorg < 10)
		eepRorg = '0' + eepRorg;
	var eepFunction = parseInt(sensorInfo[1], 10).toString(16).toUpperCase();
	if (eepFunction < 10)
		eepFunction = '0' + eepFunction;
	var eepType = parseInt(sensorInfo[2], 10).toString(16).toUpperCase();
	if (eepType < 10)
		eepType = '0' + eepType;
	getEepRorgs('0x' + eepRorg, '0x' + eepFunction, '0x' + eepType); // load
	// the
	// eeps
	// rorg

}

function getMeasureLearnSensor() {
	$
			.ajax({
				type : 'GET',
				url : PROJECT_NAME
						+ '/rest/measure_types/'
						+ $('#sensorEepRorg-1').children(":selected")
								.attr("id")
						+ '/'
						+ $('#sensorEepFunction-1').children(":selected").attr(
								"id")
						+ '/'
						+ $('#sensorEepType-1').children(":selected")
								.attr("id"),
				headers : {
					'X-Token' : $.cookie('EnoToken')
				},
				success : function(data, status, response) {
					var output = '';
					$
							.each(
									data,
									function(key, value) {
										output += '<label class="checkbox"><input name="measureTypeCheckbox" type="checkbox" value="'
												+ value.shortcut
												+ '" checked /> '
												+ value.shortcut + ' ';
										if (value.unit != null) {
											output += '[' + value.unit + '] ';
										}
										output += '</label>';
									});
					$('#measureTypeCheckboxes-1').html(output);
				},
				error : function(response, status, error) {
					console.log('Eerror : ' + error);
				},
			});
	console.log($('#sensorEepRorg-1').children(":selected").attr("id"));

}

function createLearnSensor() {
	// keep the value of al the checked checkboxes (for measure types)
	var selectedMeasures = new Array();
	$("input:checkbox[name=measureTypeCheckbox-1]:checked").each(function() {
		selectedMeasures.push($(this).val());
	});
	var measuresString = selectedMeasures.join('/'); // create an unique
	// String with all the
	// checked checkboxes

	// create a "sensor" with the input's data
	var sensor = {
		measure : measuresString,
		actuator : $('input:radio[name=sensorActuatorRadio-1]:checked').val(),
		address : $('#sensorAddress-1').val(),
		description : $('#sensorDescription-1').val(),
		eepRorg : $('#sensorEepRorg-1').val(),
		eepFunction : $('#sensorEepFunction-1').val(),
		eepType : $('#sensorEepType-1').val(),
		hybridMode : $('input:radio[name=sensorHybridRadio-1]:checked').val(),
		lastModifier : $.cookie('EnoUsr'),
		locationPath : $('#sensorPath-1').val(),
		manufacturer : $('#sensorManufacturer-1').children(":selected").val(),
		name : $('#sensorName-1').val(),
		locationId : $('#sensorLocationId-1').val(),
	};

	$.ajax({
		type : 'PUT',
		url : PROJECT_NAME + '/rest/sensor/' + $('#groupId').val(),
		contentType : 'application/json',
		data : JSON.stringify(sensor),
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		// statusCode: {
		// 401: function () {
		// window.location = "index.html";
		// },
		// 500: function () {
		// window.location = "index.html";
		// },
		// },
		success : function(data, status, response) {
			$('#addSensor').modal('hide');
			getSensorsFromId(); // refresh the list
			showAlert('#alertGroupSuccess');

			// erase the content of the inputs
			$('#sensorName-1').val('');
			$('#sensorDescription-1').val('');
			$('#sensorEepFunction-1').val('');
			$('#sensorEepType-1').val('');
			$('#sensorAddress-1').val('');
			$('#sensorPath-1').val('');
			$('#measureTypeCheckboxes-1').html('-');
		},
		error : function(response, status, error) {
			console.log(error);
		},
	});
}

//
// USERS MANAGEMENT
//

// Return all the registered users
// Call at page's loading (if the connected user is an admin)
function getUsers() {
	$
			.ajax({
				type : 'GET',
				url : PROJECT_NAME + '/rest/users',
				headers : {
					'X-Token' : $.cookie('EnoToken')
				},
				statusCode : {
					401 : function() {
						window.location = "index.html";
					},
					500 : function() {
						window.location = "index.html";
					},
				},
				success : function(data, status, response) {
					// generate the table's content
					var output = "";
					output += '<thead>';
					output += '<tr>';
					output += '<th>Username</th>';
					output += '<th>First name</th>';
					output += '<th>Last name</th>';
					output += '<th>Email</th>';
					output += '<th>Admin</th>';
					output += '<th>Active</th>';
					output += '<th></th>';
					output += '</tr>';
					output += '</thead>';
					output += '<tbody>';

					$
							.each(
									data,
									function(key, value) {
										if ($.cookie('EnoUsr') != value.username) {
											if (value.active == false) {
												output += '<tr class="error">';
											} else {
												output += '<tr>';
											}
											output += '<td>' + value.username
													+ '</td>';
											output += '<td>' + value.firstName
													+ '</td>';
											output += '<td>' + value.lastName
													+ '</td>';
											output += '<td>' + value.email
													+ '</td>';
											output += '<td>';
											if (value.admin == true) {
												output += '<i class="icon-ok"></i>';
											} else {
												output += '<i class="icon-remove"></i>';
											}
											output += '</td>';
											output += '<td>'
											if (value.active == true) {
												output += '<i class="icon-ok"></i>';
											} else {
												output += '<i class="icon-remove"></i>';
											}
											output += '</td>';
											output += '<td><a title="Edit" onClick="editUser('
													+ value.id
													+ ');"><i class="icon-edit"></i></a> / <a title="Delete" onClick="deleteUser('
													+ value.id
													+ ');"><i class="icon-remove"></i></a></td>';
											output += '</tr>';
										}
									});

					output += '</tbody>';
					$('#usersTable').html(output);
				},
				error : function(response, status, error) {
				},
			});
}

// Return the connected user's info
// Call at page's loading
function getUser() {
	$
			.ajax({
				type : 'GET',
				url : PROJECT_NAME + '/rest/user/username/'
						+ $.cookie('EnoUsr'),
				headers : {
					'X-Token' : $.cookie('EnoToken')
				},
				statusCode : {
					401 : function() {
						window.location = "index.html";
					},
					500 : function() {
						window.location = "index.html";
					},
				},
				success : function(data, status, response) {
					$('#username').val(data.username);
					$('#email').val(data.email);
					$('#firstName').val(data.firstName);
					$('#lastName').val(data.lastName);
					$('#password').val(data.password);

					var buttons = '';
					buttons += '<button type="button" class="btn" onClick="editProfile('
							+ data.id + ');">Edit</button>';
					buttons += '<button type="button" class="btn btn-danger" onClick="deleteUser('
							+ data.id + ');">Delete my account</button>';
					$('#profileButtons').html(buttons);
				},
				error : function(response, status, error) {
				},
			});
}

// Show the user pop-up and load data
function editUser(id) {
	$
			.ajax({
				type : 'GET',
				url : PROJECT_NAME + '/rest/user/' + id,
				headers : {
					'X-Token' : $.cookie('EnoToken')
				},
				statusCode : {
					401 : function() {
						window.location = "index.html";
					},
					500 : function() {
						window.location = "index.html";
					},
				},
				success : function(data, status, response) {
					// set the value
					$('#usrUsername').val(data.username);
					$('#usrPassword').val(data.password);
					$('#usrFirstName').val(data.firstName);
					$('#usrLastName').val(data.lastName);
					$('#usrEmail').val(data.email);

					var radioButtons = '';
					radioButtons += '<div class="span12">';
					radioButtons += '<label for="usrEmail">Status :</label>';
					if (data.admin) {
						radioButtons += '<label class="radio inline"><input type="radio" id="usrAdminTrue" name="usrStatus" value="true" checked> Admin </label>';
						radioButtons += '<label class="radio inline"><input type="radio" id="usrAdminFalse" name="usrStatus" value="false"> User </label>';
					} else {
						radioButtons += '<label class="radio inline"><input type="radio" id="usrAdminTrue" name="usrStatus" value="true"> Admin </label>';
						radioButtons += '<label class="radio inline"><input type="radio" id="usrAdminFalse" name="usrStatus" value="false" checked> User </label>';
					}
					radioButtons += '</div>';
					$('#statusDiv').html(radioButtons);

					var radioButtons2 = '';
					radioButtons2 += '<div class="span12">';
					radioButtons2 += '<label for="usrEmail">Account :</label>';
					if (data.active) {
						radioButtons2 += '<label class="radio inline"><input type="radio" id="usrActiveTrue" name="usrActivate" value="true" checked> Activated </label>';
						radioButtons2 += '<label class="radio inline"><input type="radio" id="usrActiveFalse" name="usrActivate" value="false"> Deactivated </label>';
					} else {
						radioButtons2 += '<label class="radio inline"><input type="radio" id="usrActiveTrue" name="usrActivate" value="true"> Activated </label>';
						radioButtons2 += '<label class="radio inline"><input type="radio" id="usrActiveFalse" name="usrActivate" value="false" checked> Deactivated </label>';
					}
					radioButtons2 += '</div>';
					$('#accountDiv').html(radioButtons2);
				},
				error : function(response, status, error) {
				},
			});

	// add the buttons "Cancel* & "Save*
	var buttons = "";
	buttons += '<button class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button>';
	buttons += '<button class="btn btn-primary" onClick="updateUser(' + id
			+ '); $(this).parent().parent().modal(\'hide\');">Save</button>';
	$('#usrButtons').html(buttons);

	$('#usrTitle').html('Edit user');

	// enable the inputs
	$('#usrFirstName').prop('disabled', false);
	$('#usrLastName').prop('disabled', false);
	$('#usrUsername').prop('disabled', false);
	$('#usrPassword').prop('disabled', false);
	$('#usrEmail').prop('disabled', false);

	// show the pop-up
	$('#manageUser').modal('show');
}

// Enable the user profile inputs and generate new buttons
function editProfile(id) {
	// enable the inputs
	$('#username').prop('disabled', false);
	$('#email').prop('disabled', false);
	$('#firstName').prop('disabled', false);
	$('#lastName').prop('disabled', false);
	$('#password').prop('disabled', false);

	// add the buttons "Cancel" and "Save changes"
	var output = '';
	output += '<div class="btn-group">';
	output += '<button type="button" class="btn" onClick="cancelProfile();">Cancel</button>';
	output += '<button type="button" class="btn btn-success" onClick="updateProfile('
			+ id + ');">Save changes</button>';
	output += '</div>';
	$('#profileButtons').html(output);
}

// Disable the inputs of the profile
function cancelProfile() {
	// diseble the inputs
	$('#username').prop('disabled', true);
	$('#email').prop('disabled', true);
	$('#firstName').prop('disabled', true);
	$('#lastName').prop('disabled', true);
	$('#password').prop('disabled', true);

	getUser();
}

// Update the user
// Param : User inputs (pop-up)
function updateUser(id) {
	// create an "user"
	var user = {
		username : $('#usrUsername').val(),
		password : $('#usrPassword').val(),
		firstName : $('#usrFirstName').val(),
		lastName : $('#usrLastName').val(),
		email : $('#usrEmail').val(),
		admin : $('input:radio[name=usrStatus]:checked').val(),
		active : $('input:radio[name=usrActivate]:checked').val()
	};

	$.ajax({
		type : 'POST',
		url : PROJECT_NAME + '/rest/user/' + id,
		contentType : 'application/json',
		data : JSON.stringify(user),
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			getUsers(); // refresh the list
		},
		error : function(response, status, error) {
		},
	});
}

// Update the profile of the connected user
// Param : User profile inputs
function updateProfile(id) {
	// create an "user"
	var user = {
		username : $('#username').val(),
		password : $('#password').val(),
		firstName : $('#firstName').val(),
		lastName : $('#lastName').val(),
		email : $('#email').val(),
		admin : $.cookie('EnoIsAd'),
		active : true
	};

	$.ajax({
		type : 'POST',
		url : PROJECT_NAME + '/rest/user/' + id,
		contentType : 'application/json',
		data : JSON.stringify(user),
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			cancelProfile();
		},
		error : function(response, status, error) {
		},
	});
}

// Delete the specified user
// Param : user id
function deleteUser(id) {
	$.ajax({
		type : 'DELETE',
		url : PROJECT_NAME + '/rest/user/' + id,
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			getUsers(); // refresh the list
		},
		error : function(response, status, error) {
		},
	});
}

// Prepare the form for adding a new user
// Call on onClick "Add a user" button
function addUser() {
	// erase the inputs' content
	$('#usrFirstName').val('');
	$('#usrLastName').val('');
	$('#usrUsername').val('');
	$('#usrPassword').val('');
	$('#usrEmail').val('');

	// enable the inputs
	$('#usrFirstName').prop('disabled', false);
	$('#usrLastName').prop('disabled', false);
	$('#usrUsername').prop('disabled', false);
	$('#usrPassword').prop('disabled', false);
	$('#usrEmail').prop('disabled', false);

	// add the buttons "Cancel* & "Save*
	var buttons = "";
	buttons += '<button class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button>';
	buttons += '<button class="btn btn-primary" onClick="createUser(); $(this).parent().parent().modal(\'hide\');">Save</button>';
	$('#usrButtons').html(buttons);

	$('#usrTitle').html('Add user');

	var radioButtons = '';
	radioButtons += '<div class="span12">';
	radioButtons += '<label for="usrEmail">Status :</label>';
	radioButtons += '<label class="radio inline"><input type="radio" id="usrAdminTrue" name="usrStatus" value="true"> Admin </label>';
	radioButtons += '<label class="radio inline"><input type="radio" id="usrAdminFalse" name="usrStatus" value="false" checked> User </label>';
	radioButtons += '</div>';
	$('#statusDiv').html(radioButtons);

	$('#accountDiv').html('');

	// show the pop-up
	$('#manageUser').modal('show');
}

// Create a new user
// Param : username, password, firstname, lastname, email, admin (from inputs)
function createUser() {
	// create an "user"
	var user = {
		username : $('#usrUsername').val(),
		password : $('#usrPassword').val(),
		firstName : $('#usrFirstName').val(),
		lastName : $('#usrLastName').val(),
		email : $('#usrEmail').val(),
		admin : $('input:radio[name=usrStatus]:checked').val()
	};

	$.ajax({
		type : 'PUT',
		url : PROJECT_NAME + '/rest/user',
		contentType : 'application/json',
		data : JSON.stringify(user),
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			getUsers(); // refresh the list
		},
		error : function(response, status, error) {
		},
	});
}

//
// SEARCH
//

// Search an existing sensor with the given text (from the input)
// Sensors can be selected using a checkbox
function searchSensorSelection() {
	var searchField = $('#searchSensorsInput').val(); // get the input value
	var myExp = new RegExp(searchField, "i"); // create an expression
	var noResults = true; // if false, we show the resluts / if true, we show
	// an alert

	$
			.ajax({
				type : 'GET',
				url : PROJECT_NAME + '/rest/sensors',
				headers : {
					'X-Token' : $.cookie('EnoToken')
				},
				success : function(data, status, response) {
					var output = '';
					var results = 0;
					// search info
					output += '<label>Results :</label>';
					// the found sensors
					output += '<form method="get" action="display.html">';
					output += '<div class="accordion" id="accordionSensors">';
					searchField = searchField.toLowerCase();
					console.log("Search :" + searchField);
					$
							.each(
									data,
									function(key, value) {
										// if the results correspond to the
										// expression, we show them (check name
										// AND location)
										if (value.name.toLowerCase().search(
												searchField) != -1
												|| value.locationPath
														.toLowerCase().search(
																searchField) != -1) {
											results++;
											var lastModification = value.lastModification; // store
											// the
											// last
											// modification's
											// date
											// for
											// spliting
											// operation
											console.log("Result : "
													+ value.name);

											output += '<div class="accordion-group">';
											output += '<div class="accordion-heading">';
											output += '<label class="checkbox displayLabel"><input type="checkbox" name="sens'
													+ value.id
													+ '" value="1"/>';
											output += '<a class="accordion-toggle displayCheckbox" data-toggle="collapse" data-parent="#accordionSensors" href="#existingCollapse'
													+ value.id
													+ '">'
													+ value.name + '</a>';
											output += '</label></div>';
											output += '<div id="existingCollapse'
													+ value.id
													+ '" class="accordion-body collapse">';
											output += '<div class="accordion-inner">';
											output += '<div class="row-fluid">';
											// details table
											output += '<table>';
											output += '<tr><td>Name :</td><td>'
													+ value.name
													+ '</td><td>EEP rorg :</td><td>'
													+ value.eepRorg
															.toString(16)
															.toUpperCase()
													+ '</td></tr>';
											output += '<tr><td>Description :</td><td>'
													+ value.description
													+ '</td><td>EEP function :</td><td>'
													+ value.eepFunction
															.toString(16)
															.toUpperCase()
													+ '</td></tr>';
											output += '<tr><td>Location :</td><td>'
													+ value.locationPath
													+ '</td><td>EEP type :</td><td>'
													+ value.eepType
															.toString(16)
															.toUpperCase()
													+ '</td></tr>';
											output += '<tr><td>Manufacturer :</td><td>'
													+ value.manufacturer
													+ '</td><td>Hybrid mode :</td><td>'
													+ (value.hybridMode == true ? 'Yes'
															: 'No')
													+ '</td></tr>';
											output += '<tr><td>Last modification :</td><td>'
													+ lastModification.slice(8,
															10)
													+ '.'
													+ lastModification.slice(5,
															7)
													+ '.'
													+ lastModification.slice(0,
															4)
													+ '</td><td>by :</td><td>'
													+ value.lastModifier
													+ '</td></tr>';
											output += '</table>';
											output += '</div>';
											output += '</div>';
											output += '</div>';
											output += '</div>';
											output += '</div>';
										}
									});
					if (results == 0) {
						output = '<div id="alertNoGroupError" class="alert alert-info"><button type="button" class="close" onClick="$(this).parent().hide();">&times;</button><strong>Heads up!</strong> There are no results for your search.</div>';
					} else {
						output += '<div class="form-search"><div class="input-append">';
						output += '<label class="inline"> From <input type="text" name="dateFrom" id="dateFrom" /></label><label class="inline"> To <input type="text" name="dateTo" id="dateTo" /></label>';
						output += "</div></div>";
						output += '<button class="btn btn-primary displayButton" type="submit">display</button>';
						output += '</form>';
						output += '</div>';
					}
					$('#searchSensor').html(output);
					$('#dateFrom').datetimepicker({dateFormat: 'yy-mm-dd', timeFormat: 'HH:mm:ss', autoSize: true}).datetimepicker('setDate', '-1d');
					$('#dateTo').datetimepicker({dateFormat: 'yy-mm-dd', timeFormat: 'HH:mm:ss', autoSize: true}).datetimepicker('setDate', new Date());
				},
				error : function(response, status, error) {
					console.log('Error : ' + error);
				},
			});
}

// Search an existing group with the given text (from the input)
// Groups can be selected using a radio button
function searchGroups() {
	var searchField = $('#searchGroupsInput').val(); // get the input value
	var myExp = new RegExp(searchField, "i"); // create an expression
	var noResults = true; // if false, we show the resluts / if true, we show
	// an alert

	$
			.ajax({
				type : 'GET',
				url : PROJECT_NAME + '/rest/groups',
				headers : {
					'X-Token' : $.cookie('EnoToken')
				},

				success : function(data, status, response) {
					var output = '';
					var results = 0;
					// search info
					output += '<label>Results :</label>';
					// the found groups
					output += '<div class="accordion" id="accordionGroups">';
					searchField = searchField.toLowerCase();
					console.log("Search :" + searchField);
					$
							.each(
									data,
									function(key, value) {
										// if the results correspond to the
										// expression, we show them
										if (value.name.toLowerCase().search(
												searchField) != -1) {
											results++;
											console.log("Result : "
													+ value.name);

											output += '<div class="accordion-group">';
											output += '<div class="accordion-heading">';
											output += '<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordionGroups" onMouseDown="document.getElementById(\'radio'
													+ value.id
													+ '\').checked ? document.getElementById(\'radio'
													+ value.id
													+ '\').checked = false : document.getElementById(\'radio'
													+ value.id
													+ '\').checked =true;" href="#existingGroupCollapse'
													+ value.id + '">';
											output += ' <label class="radio inline"><input id="radio'
													+ value.id
													+ '" type="radio" name="selGroup" />'
													+ value.name
													+ '</a></label>';
											output += '</div>';
											output += '<div id="existingGroupCollapse'
													+ value.id
													+ '" class="accordion-body collapse">';
											output += '<div class="accordion-inner">';
											output += '<div class="row-fluid">';
											// details table
											output += '<div id="groupsDetails'
													+ value.id + '"></div>';
											output += '<form method="GET" action="display.html">';
											output += '<div id="groupsSensors'
													+ value.id + '"></div>';
											output += '<div class="form-search"><div class="input-append">';
											output += '<label class="inline"> From <input type="text" name="dateFrom" id="dateFrom2" /></label><label class="inline"> To <input type="text" name="dateTo" id="dateTo2" /></label>';
											output += "</div></div>";
											output += '<button class="btn btn-primary displayButton" type="submit">display</button>';
											output += '</form>';
											// get informations for the group
											getGroupDetails(value.id);
											// get sensors for the group
											getGroupSensors(value.id);
											output += '</div>';
											output += '</div>';
											output += '</div>';
											output += '</div>';
											output += '</div>';

										}
									});
					if (results == 0) {
						output = '<div id="alertNoGroupError" class="alert alert-info"><button type="button" class="close" onClick="$(this).parent().hide();">&times;</button><strong>Heads up!</strong> There are no results for your search.</div>';
					} else {
						output += '</div>';
					}
					$('#searchGroup').html(output);
					$('#dateFrom2').datetimepicker({dateFormat: 'yy-mm-dd', timeFormat: 'HH:mm:ss', autoSize: true}).datetimepicker('setDate', '-24h');
					$('#dateTo2').datetimepicker({dateFormat: 'yy-mm-dd', timeFormat: 'HH:mm:ss', autoSize: true}).datetimepicker('setDate', new Date());
				},
				error : function(response, status, error) {
					console.log('Error : ' + error);
				},
			});
}

// Get the given group's informations
// Param : group id
function getGroupDetails(id) {
	$.ajax({
		type : 'GET',
		url : PROJECT_NAME + '/rest/group/' + id,
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		statusCode : {
			401 : function() {
				window.location = "index.html";
			},
			500 : function() {
				window.location = "index.html";
			},
		},
		success : function(data, status, response) {
			var lastModification = data.lastModification; // get the last
			// modification's
			// data for the
			// spliting
			// operatons

			// set the div with the given group's information
			var output = '';
			output += '<table>';
			output += '<tr><td>Name :</td><td>' + data.name + '</td></tr>';
			output += '<tr><td>Description :</td><td>' + data.description
					+ '</td></tr>';
			output += '<tr><td>Last modification :</td><td>'
					+ lastModification.slice(8, 10) + '.'
					+ lastModification.slice(5, 7) + '.'
					+ lastModification.slice(0, 4) + ' by ' + data.lastModifier
					+ '</td></tr>';
			output += '</div>';
			$('#groupsDetails' + id).html(output);
		},
		error : function(response, status, error) {
			showAlert('#alertGroupError');
		},
	});
}

// Get the sensors from the group id
// Param : group id
function getGroupSensors(id) {
	$
			.ajax({
				type : 'GET',
				url : PROJECT_NAME + '/rest/sensors/' + id,
				headers : {
					'X-Token' : $.cookie('EnoToken')
				},
				statusCode : {
					401 : function() {
						window.location = "index.html";
					},
					500 : function() {
						window.location = "index.html";
					},
				},
				success : function(data, status, response) {
					var isEmpty = isEmptyObject(data); // check the object

					if (isEmpty == true) {
						// remove the content of the DIVs
						$('#sensors').html('');
						$('#groupLegend').html('');
						showAlert('#alertNoSensorError');
					} else {
						var output = '';
						var noAccordion = $('#noAccordion').val();
						output += '<div class="accordion" id="accordion'
								+ noAccordion + '">';

						$
								.each(
										data,
										function(key, value) {
											var lastModification = value.lastModification; // store
											// the
											// last
											// modification's
											// date
											// for
											// spliting
											// operation
											var actuator = '';
											if (value.actuator == true) {
												actuator += '( actuator )';
											}
											; // if the sensor is an
											// acttuator, we add
											// "(actuator)" on the sensor's
											// title

											// accordion head
											output += '<div class="accordion-group">';
											output += '<div class="accordion-heading">';
											output += '<label class="checkbox displayLabel"><input type="checkbox" name="sens'
													+ value.id
													+ '" value="1"/>';
											output += '<a class="accordion-toggle displayCheckbox" data-toggle="collapse" data-parent="#accordion'
													+ noAccordion
													+ '" href="#collapse'
													+ id
													+ value.id
													+ '">'
													+ value.name
													+ ' '
													+ actuator + '</a>';
											output += '</label></div>';

											// accordion body
											output += '<div id="collapse'
													+ id
													+ value.id
													+ '" class="accordion-body collapse">';
											output += '<div class="accordion-inner">';
											output += '<div class="row-fluid">';
											output += '<div class="span6">';
											// details table
											output += '<table>';
											output += '<tr><td>Name :</td><td>'
													+ value.name
													+ '</td><td>EEP rorg :</td><td>'
													+ value.eepRorg
															.toString(16)
															.toUpperCase()
													+ '</td></tr>';
											output += '<tr><td>Description :</td><td>'
													+ value.description
													+ '</td><td>EEP function :</td><td>'
													+ value.eepFunction
															.toString(16)
															.toUpperCase()
													+ '</td></tr>';
											output += '<tr><td>Location :</td><td>'
													+ value.locationPath
													+ '</td><td>EEP type :</td><td>'
													+ value.eepType
															.toString(16)
															.toUpperCase()
													+ '</td></tr>';
											output += '<tr><td>Manufacturer :</td><td>'
													+ value.manufacturer
													+ '</td><td>Hybrid mode :</td><td>'
													+ (value.hybridMode == true ? 'Yes'
															: 'No')
													+ '</td></tr>';
											output += '<tr><td>Last modification :</td><td>'
													+ lastModification.slice(8,
															10)
													+ '.'
													+ lastModification.slice(5,
															7)
													+ '.'
													+ lastModification.slice(0,
															4)
													+ '</td><td>by :</td><td>'
													+ value.lastModifier
													+ '</td></tr>';
											output += '</table>';
											output += '</div>';
											output += '</div>';
											output += '</div>';
											output += '</div>';
											output += '</div>';
										});
						output += '</div>';
						noAccordion++;
						$('#noAccordion').val(noAccordion);
						$('#groupsSensors' + id).html(output);
					}
				},
				error : function(response, status, error) {
					showAlert('#alertGroupError');
				},
			});
}

//
// GRAPHS
//

/**
 * Fonction de récupération des paramètres GET de la page
 * 
 * @return Array Tableau associatif contenant les paramètres GET
 */
function extractUrlParams() {
	var t = location.search.substring(1).split('&');
	var f = {};
	for ( var i = 0; i < t.length; i++) {
		var x = t[i].split('=');
		f[x[0]] = x[1];
	}
	return f;
}

// Get the selected sensor's informations
// Displays graphs
function displayGraphs() {
	var urlParams = location.search.substring(1).split('&');
	var output = '';
	var sensorIds = []; // selected sensors
	var types = []; // all measure types used
	var units = []; // all units
	var measureTypes = []; // measure types for each sensors
	var dateFrom, dateTo;

	// get the url parameters
	var urlParams = extractUrlParams();
	for (key in urlParams) {
		if (key.indexOf('sens') > -1)
			sensorIds[sensorIds.length] = parseInt(key.substring(4));
		if (key == "dateFrom")
			dateFrom = urlParams[key];
		if (key == "dateTo")
			dateTo = urlParams[key];
	}

	/*
	 * // split the URL parameters to get the sensors ids for ( var i = 0; i <
	 * urlParams.length; i++) { var param = urlParams[i].split('=');
	 * sensorIds[i] = param[0].substring(4); }
	 */

	var sensors = getSensors();

	// get the measure types for all sensors
	for (i = 0; i < sensorIds.length; i++) {
		types = getTypes(sensorIds[i], types, sensors);
		units = getUnits(sensorIds[i], units, sensors);
		measureTypes[sensorIds[i]] = getTypesForSensor(sensorIds[i], sensors);
	}

	// CSV
	for (i = 0; i < types.length; i++) {
		var datasId = '';

		// get the data ids for the csv
		for ( var j = 0; j < sensorIds.length; j++) {
			for ( var k = 0; k < measureTypes[sensorIds[j]].length; k++) {
				if (types[i] == measureTypes[sensorIds[j]][k]) {
					if (datasId != '')
						datasId += ';';
					datasId += getMeasureId(sensorIds[j],
							measureTypes[sensorIds[j]][k], sensors);
				}
			}
		}
		// link to download the csv
		if (datasId.indexOf(";") != -1)
			output += '<div class="csv"><a onClick="DownloadJSON2CSV(getMultipleJson(\''
					+ datasId + '\'))">download CSV</a></div>';
		else
			output += '<div class="csv"><a onClick="DownloadJSON2CSV(getJsonDatas('
					+ datasId + '))">download CSV</a></div>';
		// create a container for each graph
		output += '<div id="container'
				+ i
				+ '" style="margin-bottom:30px; padding-bottom:30px; border-bottom:1px solid #E5E5E5"/>';
	}
	$('#graphs').html(output);

	for (i = 0; i < types.length; i++) {
		var subtitle = '';
		// create a graph for each measure type using highcharts
		$('#container' + i).highcharts('StockChart', {
			title : {
				text : types[i] + ' (' + units[i] + ')',
				x : -20
			// center
			},
			subtitle : {
				text : '',
				x : -20
			},
			credits : {
				enabled : false
			},
			navigator : {
				enabled : true
			},
			scrollbar : {
				enabled : false
			},
			rangeSelector : {
				buttons : [ {
					type : 'day',
					count : 1,
					text : '1d'
				}, {
					type : 'week',
					count : 1,
					text : '1w'
				}, {
					type : 'month',
					count : 1,
					text : '1m'
				}, {
					type : 'all',
					text : 'All'
				} ],
				inputStyle: {
		    		fontSize: '8px'
		    	},
				enabled : true,
				inputDateFormat: '%Y-%m-%e %H:%M:%S',
	            inputEditDateFormat: '%Y-%m-%e %H:%M:%S'
			},
			yAxis : {
				title : {
					text : types[i] + ' (' + units[i] + ')'
				},
				plotLines : [ {
					value : 0,
					width : 1,
					color : '#808080'
				} ]
			},
			tooltip : {
				valueSuffix : units[i]
			},
			legend : {
				enabled : true,
				layout : 'vertical',
				align : 'right',
				verticalAlign : 'middle',
				borderWidth : 0
			}
		});

		// get the data for the graph
		for ( var j = 0; j < sensorIds.length; j++) {
			// check if the sensor has this measure type
			for ( var k = 0; k < measureTypes[sensorIds[j]].length; k++) {
				if (types[i] == measureTypes[sensorIds[j]][k]) {
					// adds the data to the graph
					var sensorName = getSensorName(sensorIds[j], sensors);
					subtitle += sensorName + ' ';
					var datas = getDatas(getMeasureId(sensorIds[j],
							measureTypes[sensorIds[j]][k], sensors), dateFrom,
							dateTo);
					var chart = $('#container' + i).highcharts();
					chart.addSeries({
						name : sensorName,
						data : datas
					});
					chart.rangeSelector.clickButton(3, {}, true);

				}
			}
			// changes chart's subtitle with sensors names
			chart.setTitle(null, {
				text : subtitle
			});
		}
	}
}

// get all different types
// param : sensor id
// param : types array
// param : all sensors sensors
// return : types array with all types
function getTypes(id, types, sensors) {
	$.each(sensors, function(key, value) {
		// if the results correspond to the id, we select them
		if (value.id == id) {
			$.each(value['measure'], function(key, measure) {
				var newType = true;
				// for every type of measure check if it's new / if true
				// type is added to the array
				for ( var i = 0; i < types.length; i++) {
					if (types[i] == measure.eepShortcut) {
						newType = false;
					}
				}
				if (newType == true)
					types[types.length] = measure.eepShortcut;
			});
		}
	});

	return types;
}

// get all different units corresponding to the types
// param : sensor id
// param : units array
// param : all sensors sensors
// return : units array with all units
function getUnits(id, units, sensors) {
	$.each(sensors, function(key, value) {
		// if the results correspond to the id, we select them
		if (value.id == id) {
			$.each(value['measure'], function(key, measure) {
				var newUnit = true;
				// for every unit check if it's new / if true type is
				// added to the array
				for ( var i = 0; i < units.length; i++) {
					if (units[i] == measure.unit) {
						newUnit = false;
					}
				}
				if (newUnit == true)
					units[units.length] = measure.unit;
			});
		}
	});
	return units;
}

// get the different types for a given sensor
// param : sensor id
// param : all sensors sensors
// return : measureTypes array with all the measure types
function getTypesForSensor(id, sensors) {
	var measureTypes = [];

	// all sensors
	$.each(sensors, function(key, value) {
		// if the results correspond to the id,
		// we select them and create an array
		// with the types
		if (value.id == id) {
			$.each(value['measure'], function(key, measure) {
				measureTypes[measureTypes.length] = measure.eepShortcut;
			});
		}
	});

	return measureTypes;
}

// get the name of a given sensor
// param : sensor id
// param : all sensors sensors
// return : name
function getSensorName(id, sensors) {
	var name = '';

	$.each(sensors, function(key, value) {
		// if the results correspond to the id, we select them
		if (value.id == id) {
			name = value.name;
		}
	});

	return name;
}

// get all the sensors
function getSensors() {
	var sensors;
	$.ajax({
		type : 'GET',
		async : false,
		url : PROJECT_NAME + '/rest/sensors',
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		success : function(data, status, response) {
			sensors = data;
		}
	});
	return sensors;
}

// get highchart formatted data for the given measure id
// param : measure id
// param : dateFrom Start date for data
// param : dateTo End date for data
// return : datas highchart formatted array of data
function getDatas(id, dateFrom, dateTo) {
	var datas = [];
	var i = 0;
	$.ajax({
		type : 'GET',
		async : false,
		url : PROJECT_NAME + '/rest/datas/' + id,
		data : 'from=' + dateFrom + '&to=' + dateTo,
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		success : function(data, status, response) {
			// the found data
			$.each(data, function(key, value) {
				// var date = value.date.split("-");
				var date = new Date(value.date);
				var localDate = new Date();
				localDate.setUTCDate(date.getDate());
				localDate.setUTCFullYear(date.getFullYear());
				localDate.setUTCMonth(date.getMonth());
				localDate.setUTCHours(date.getHours());
				localDate.setUTCMinutes(date.getMinutes());
				localDate.setUTCSeconds(date.getSeconds());
				localDate.setMilliseconds(date.getMilliseconds());
				datas[i] = [ localDate.getTime(), value.value ];
				i++;
			});
		}
	});
	return datas;
}

// get the measure id for a given measure
// param : sensor id
// param : measure value
// param : all sensors sensors
// return : id of the wanted measure
function getMeasureId(sensorId, measureValue, sensors) {
	var id = '';

	// all sensors
	$.each(sensors, function(key, value) {
		// if the results correspond to the given id, we select them
		if (value.id == sensorId) {
			$.each(value['measure'], function(key, measure) {
				// if the measure value correspond, we select the id
				if (measureValue == measure.eepShortcut) {
					id = measure.id;
				}
			});
		}
	});

	return id;
}

// get the JSON data for a given measure id
// param : measure id
// return : datas JSON data
function getJsonDatas(id) {
	var datas = '';
	$.ajax({
		type : 'GET',
		async : false,
		url : PROJECT_NAME + '/rest/datas/' + id,
		data : 'from=' + dateFrom + '&to=' + dateTo,
		headers : {
			'X-Token' : $.cookie('EnoToken')
		},
		success : function(data, status, response) {
			// the found data
			datas = data;
		}
	});
	return datas;
}

// get the JSON data for multiple given measure ids
// param : measure id separate by a ";"
// return : data JSON merged data
function getMultipleJson(datasId) {
	var idTab = datasId.split(";");
	var data = [];
	// for each measure id, we take the corresponding JSON
	for ( var j = 0; j < idTab.length; j++) {
		// if data[0] isn't empty, merge the data with the older ones
		if (data[0] != null)
			data[0] = jQuery.merge(data[0], getJsonDatas(idTab[j]));
		else
			data[0] = getJsonDatas(idTab[j]);
	}
	return data[0];
}

// download the csv
// param : objArray
function DownloadJSON2CSV(objArray) {
	var array = typeof objArray != 'object' ? JSON.parse(objArray) : objArray;
	var str = '';
	var lineBreak = '';
	if (navigator.appName != 'Microsoft Internet Explorer')
		lineBreak = "%0A%0D";
	else
		lineBreak = "\r\n"

		// header
	for ( var i = 0; i < 1; i++) {
		for ( var index in array[i]) {
			str += [ index ] + ";";
		}
	}
	str += lineBreak;

	// lines
	for ( var i = 0; i < array.length; i++) {
		var line = '';
		for ( var index in array[i]) {
			if (line != '')
				line += ';'

			line += array[i][index];
		}
		str += line + lineBreak;
	}

	if (navigator.appName != 'Microsoft Internet Explorer') {
		var uri = 'data:text/csv;charset=utf-8,' + str;
		var downloadLink = document.createElement("a");
		downloadLink.href = uri;
		downloadLink.download = "data.csv";
		document.body.appendChild(downloadLink);
		downloadLink.click();
	} else {
		var popup = window.open('', 'csv', '');
		popup.document.body.innerHTML = '<pre>' + str + '</pre>';
	}
}
