"use strict";

let fs = require('fs');

let commandFileName = "command.txt";
let stateFileName = "state.json";

let key = "";
let workingDirectory = "";
let stateFile = "";
let myself = "";
let opponent = "";
let gameMap = "";
let mapSize = "";
let cells = "";
let buildings = "";
let missiles = "";
let buildingStats = [];

// Capture the arguments
initBot(process.argv.slice(2));

function initBot(args) {
    key = args[0];
    workingDirectory = args[1];

    // Read the current state and choose an action
    stateFile = require('./' + stateFileName);

    myself = stateFile.players.filter(p => p.playerType === 'A')[0];
    opponent = stateFile.players.filter(p => p.playerType === 'B')[0];
    mapSize = {
        x: stateFile.gameDetails.mapWidth,
        y: stateFile.gameDetails.mapHeight
    };

    let stats = stateFile.gameDetails.buildingsStats;
	buildingStats[0]= stats.DEFENSE;
	buildingStats[1]= stats.ATTACK;
	buildingStats[2]= stats.ENERGY;

    gameMap = stateFile.gameMap;
    initEntities();

    runStrategy();
}

function initEntities() {
    cells = flatMap(gameMap); // all cells on the entire map

    buildings = cells.filter(cell => cell.buildings.length > 0).map(cell => cell.buildings);
    buildings = flatMap(buildings); // flat array of everyone's buildings

    missiles = cells.filter(cell => cell.missiles.length > 0).map(cell => cell.missiles);
    missiles = flatMap(missiles); // flat array of everyone's missiles
}

function runStrategy() {
    if (isUnderAttack()) {
        defendRow();
    } else if (hasEnoughEnergyForMostExpensiveBuilding()) {
        buildRandom();
    } else {
        doNothingCommand();
    }
}

function isUnderAttack() {
    // is there a row under attack? and have enough energy to build defence?
	let myDefenders = buildings.filter(b => b.playerType == 'A' && b.buildingType == 'DEFENSE');
    let opponentAttackers = buildings.filter(b => b.playerType == 'B' && b.buildingType == 'ATTACK')
									 .filter(b => !myDefenders.some(d => d.y == b.y));
    
    return (opponentAttackers.length > 0) && (myself.energy >= buildingStats[0].price);
}

function defendRow() {
    // is there a row under attack? and have enough energy to build defence?
	let myDefenders = buildings.filter(b => b.playerType == 'A' && b.buildingType == 'DEFENSE');
    let opponentAttackers = buildings.filter(b => b.playerType == 'B' && b.buildingType == 'ATTACK')
									 .filter(b => !myDefenders.some(d => d.y == b.y));
	if (opponentAttackers.length == 0) {
		buildRandom();
        return
	}
    // choose the first row with an opponent attacker
    let rowNumber = opponentAttackers[0].y;
    // get all the x-coordinates for this row, that are empty
    let emptyCells = cells.filter(c => c.buildings.length == 0 && c.x <= (mapSize.x / 2) - 1 && c.y == rowNumber);
    if (emptyCells.length == 0) {
        // cannot build there, try to build somewhere else
        buildRandom();
        return
    }

    let command = {x: '', y: '', bt: ''};
    command.x = getRandomFromArray(emptyCells).x;
    command.y = rowNumber;
    command.bt = 0; // defence building
    buildCommand(command.x, command.y, command.bt);
}

function hasEnoughEnergyForMostExpensiveBuilding() {
    return (myself.energy >= Math.max(...(buildingStats.map(stat => stat.price))));
}

function buildRandom() {
    // cells without buildings on them, and on my half of the map
    let emptyCells = cells.filter(c => c.buildings.length == 0 && c.x <= (mapSize.x / 2) - 1);
	if (emptyCells.length == 0) {
		doNothingCommand();
        return;
	}
    let randomCell = getRandomFromArray(emptyCells);

    let command = {x: '', y: '', bt: ''};
    command.x = randomCell.x;
    command.y = randomCell.y;
    command.bt = getRandomInteger(2);
    buildCommand(command.x, command.y, command.bt);
}

function buildCommand(x, y, bt) {
    writeToFile(commandFileName, `${x},${y},${bt}`);
}

function doNothingCommand() {
    writeToFile(commandFileName, ``);
}

function writeToFile(fileName, payload) {
    fs.writeFile('./' + fileName, payload, function (err) {
        if (err) {
            return console.log(err);
        }
        // console.log(payload);
    });
}

/***
 * Returns an array with one less level of nesting
 * @param array
 * @returns {Array}
 */
function flatMap(array) {
    return array.reduce((acc, x) => acc.concat(x), []);
}

/***
 * Returns a random integer between 0(inclusive) and max(inclusive)
 * @param max
 * @returns {number}
 */
function getRandomInteger(max) {
    return Math.round(Math.random() * max);
}

/**
 * Returns an array that is filled with integers from 0(inclusive) to count(inclusive)
 * @param count
 * @returns {number[]}
 */
function getArrayRange(count) {
    return Array.from({length: count}, (v, i) => i);
}

/**
 * Return a random element from a given array
 * @param array
 * @returns {*}
 */
function getRandomFromArray(array) {
    return array[Math.floor((Math.random() * array.length))];
}
