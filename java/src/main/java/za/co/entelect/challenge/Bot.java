package za.co.entelect.challenge;

import za.co.entelect.challenge.entities.Building;
import za.co.entelect.challenge.entities.CellStateContainer;
import za.co.entelect.challenge.entities.GameState;
import za.co.entelect.challenge.entities.BuildingsOnMap;
import za.co.entelect.challenge.enums.BuildingType;
import za.co.entelect.challenge.enums.PlayerType;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Bot {

    private GameState gameState;
    private BuildingsOnMap counterBuilding;

    /**
     * Constructor
     *
     * @param gameState the game state
     **/
    public Bot(GameState gameState) {
        this.gameState = gameState;
        gameState.getGameMap();
        this.counterBuilding = new BuildingsOnMap(gameState, PlayerType.A);
    }

    /**
     * Run
     *
     * @return the result
     **/
    public String run() {
        String command = "";
        for(int i = 0; i <gameState.gameDetails.mapHeight; i++){
            if(counterBuilding.building[1][i].a>3 && counterBuilding.building[0][i].d<1){
                if(canAffordBuilding(BuildingType.DEFENSE)){
                    command = placeBuildingInRowFromFront(BuildingType.DEFENSE, i);
                }
                break;
            }
        }

        for (int i=0;i<gameState.gameDetails.mapHeight;i++){
            if(counterBuilding.building[1][i].d == 0 && command==""){
                if(canAffordBuilding(BuildingType.ATTACK)){
                    command = placeBuildingInRowFromFront(BuildingType.ATTACK, findOnlyEnergyEnemy());
                }
                break;
            }
        }

        if(canAffordBuilding(BuildingType.ATTACK) && command == ""){
            command = placeBuildingInRowFromFront(BuildingType.ATTACK, 7);
        }

        for(int i = 0; i < gameState.gameDetails.mapHeight; i++){
            if(counterBuilding.building[0][i].e == 0 && counterBuilding.building[1][i].a == 0){
                if(canAffordBuilding(BuildingType.ENERGY) && command == ""){
                    command = placeBuildingInRowFromBack(BuildingType.ENERGY, i);
                }
                break;
            }
        }

        return command;
    }

    /**
     * Place building in a random row nearest to the back
     *
     * @param buildingType the building type
     * @return the result
     **/
    private String placeBuildingRandomlyFromBack(BuildingType buildingType) {
        for (int i = 0; i < gameState.gameDetails.mapWidth / 2; i++) {
            List<CellStateContainer> listOfFreeCells = getListOfEmptyCellsForColumn(i);
            if (!listOfFreeCells.isEmpty()) {
                CellStateContainer pickedCell = listOfFreeCells.get((new Random()).nextInt(listOfFreeCells.size()));
                return buildCommand(pickedCell.x, pickedCell.y, buildingType);
            }
        }
        return "";
    }

    /**
     * Place building in a random row nearest to the front
     *
     * @param buildingType the building type
     * @return the result
     **/
    private String placeBuildingRandomlyFromFront(BuildingType buildingType) {
        for (int i = (gameState.gameDetails.mapWidth / 2) - 1; i >= 0; i--) {
            List<CellStateContainer> listOfFreeCells = getListOfEmptyCellsForColumn(i);
            if (!listOfFreeCells.isEmpty()) {
                CellStateContainer pickedCell = listOfFreeCells.get((new Random()).nextInt(listOfFreeCells.size()));
                return buildCommand(pickedCell.x, pickedCell.y, buildingType);
            }
        }
        return "";
    }

    private int findOnlyEnergyEnemy(){
        int val = 0;
        for(int i = 0; i < gameState.gameDetails.mapHeight; i++){
            if(counterBuilding.building[1][i].e != 0
                    && counterBuilding.building[1][i].a == 0
                    && counterBuilding.building[1][i].d == 0){
                val = i;
            }
        }
        return val;
    }

    private int findMaximumAttack(){
        int max;
        int val = 0;
        do{
            max = counterBuilding.building[0][val].a;
            val++;
        }while (max >= 6 && val < gameState.gameDetails.mapHeight);

        if(max >= 6){
            return 99;
        }

        else{
            for(int i = val; i < gameState.gameDetails.mapHeight; i++){
                if(counterBuilding.building[0][i].a > max && counterBuilding.building[0][i].a < 6){
                    val = i;
                    max = counterBuilding.building[0][i].a;
                }
            }
            val--;
            return val;
        }
    }

    private String placeBuildingFront(BuildingType buildingType, int y){
        return buildCommand((gameState.gameDetails.mapWidth/2) - 1, y, buildingType);
    }
    /**
     * Place building in row y nearest to the front
     *
     * @param buildingType the building type
     * @param y            the y
     * @return the result
     **/

    private String placeBuildingInRowFromFront(BuildingType buildingType, int y) {
        for (int i = (gameState.gameDetails.mapWidth / 2) - 1; i >= 0; i--) {
            if (isCellEmpty(i, y)) {
                return buildCommand(i, y, buildingType);
            }
        }
        return "";
    }

    /**
     * Place building in row y nearest to the back
     *
     * @param buildingType the building type
     * @param y            the y
     * @return the result
     **/
    private String placeBuildingInRowFromBack(BuildingType buildingType, int y) {
        for (int i = 0; i < gameState.gameDetails.mapWidth / 2; i++) {
            if (isCellEmpty(i, y)) {
                return buildCommand(i, y, buildingType);
            }
        }
        return "";
    }

    private int findMaxAttackEnemy(){
        int answer = -999;
        int max = 0;
        for(int i = 0; i < gameState.gameDetails.mapHeight; i++){
            if(counterBuilding.building[1][i].a > max){
                answer = i;
                max = counterBuilding.building[1][i].a;
            }
        }
        return answer;
    }

    /**
     * Construct build command
     *
     * @param x            the x
     * @param y            the y
     * @param buildingType the building type
     * @return the result
     **/
    private String buildCommand(int x, int y, BuildingType buildingType) {
        return String.format("%s,%d,%s", String.valueOf(x), y, buildingType.getCommandCode());
    }

    /**
     * Get all buildings for player in row y
     *
     * @param playerType the player type
     * @param filter     the filter
     * @param y          the y
     * @return the result
     **/
    private List<Building> getAllBuildingsForPlayer(PlayerType playerType, Predicate<Building> filter, int y) {
        return gameState.getGameMap().stream()
                .filter(c -> c.cellOwner == playerType && c.y == y)
                .flatMap(c -> c.getBuildings().stream())
                .filter(filter)
                .collect(Collectors.toList());
    }

    /**
     * Get all empty cells for column x
     *
     * @param x the x
     * @return the result
     **/
    private List<CellStateContainer> getListOfEmptyCellsForColumn(int x) {
        return gameState.getGameMap().stream()
                .filter(c -> c.x == x && isCellEmpty(x, c.y))
                .collect(Collectors.toList());
    }

    /**
     * Checks if cell at x,y is empty
     *
     * @param x the x
     * @param y the y
     * @return the result
     **/
    private boolean isCellEmpty(int x, int y) {
        Optional<CellStateContainer> cellOptional = gameState.getGameMap().stream()
                .filter(c -> c.x == x && c.y == y)
                .findFirst();

        if (cellOptional.isPresent()) {
            CellStateContainer cell = cellOptional.get();
            return cell.getBuildings().size() <= 0;
        } else {
            System.out.println("Invalid cell selected");
        }
        return true;
    }

    /**
     * Checks if building can be afforded
     *
     * @param buildingType the building type
     * @return the result
     **/
    private boolean canAffordBuilding(BuildingType buildingType) {
        return getEnergy(PlayerType.A) >= getPriceForBuilding(buildingType);
    }

    /**
     * Gets energy for player type
     *
     * @param playerType the player type
     * @return the result
     **/
    private int getEnergy(PlayerType playerType) {
        return gameState.getPlayers().stream()
                .filter(p -> p.playerType == playerType)
                .mapToInt(p -> p.energy)
                .sum();
    }

    /**
     * Gets price for building type
     *
     * @param buildingType the player type
     * @return the result
     **/
    private int getPriceForBuilding(BuildingType buildingType) {
        return gameState.gameDetails.buildingsStats.get(buildingType).price;
    }

    /**
     * Gets price for most expensive building type
     *
     * @return the result
     **/
    private int getMostExpensiveBuildingPrice() {
        return gameState.gameDetails.buildingsStats
                .values().stream()
                .mapToInt(b -> b.price)
                .max()
                .orElse(0);
    }
}
