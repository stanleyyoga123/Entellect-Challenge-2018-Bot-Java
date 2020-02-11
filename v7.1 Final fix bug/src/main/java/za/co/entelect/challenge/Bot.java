
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
    // fields
    private GameState gameState;
    private BuildingsOnMap counterBuilding;

    // constructor
    public Bot(GameState gameState) {
        this.gameState = gameState;
        gameState.getGameMap();
        this.counterBuilding = new BuildingsOnMap(gameState, PlayerType.A);
    }
    // main bot
    public String run() {
        String command = "";
        // membangun bangunan defense jika energi cukup
        if(canAffordBuilding(BuildingType.DEFENSE)){
            // menempatkan bangunan di row paling depan
            for(int i = 0; i <gameState.gameDetails.mapHeight; i++){
                if(counterBuilding.building[1][i].a>3 && counterBuilding.building[0][i].d<1){
                    command = placeBuildingInRowFromFront(BuildingType.DEFENSE, i);
                }
                break;
            }
        }
        // membangun bangunan attack jika energi cukup
        if(canAffordBuilding(BuildingType.ATTACK) && command == ""){
            for (int i=0;i<gameState.gameDetails.mapHeight;i++){
                // menempatkan bangunan di baris paling depan apabila terdapat defenses
                if(counterBuilding.building[1][i].d == 0){
                    if(counterBuilding.building[0][i].a + counterBuilding.building[0][i].d + counterBuilding.building[0][i].e < 8){
                        command = placeBuildingInRowFromFront(BuildingType.ATTACK, i);
                    }
                    break;
                }
            }
        }
        
        // membangun bangunan attack jika energi cukup
        if(canAffordBuilding(BuildingType.ATTACK) && command == ""){
            for(int i = 0; i < gameState.gameDetails.mapHeight;i++){
                //menempatkan bangunan di sel yang kosong
                if(counterBuilding.building[0][i].a + counterBuilding.building[0][i].d + counterBuilding.building[0][i].e < 8){
                    command = placeBuildingInRowFromFront(BuildingType.ATTACK, i);
                }
                break;
            }
        }
        // membangun bangunan penghasil energi jika energi cukup
        if(canAffordBuilding(BuildingType.ENERGY) && command == ""){
            // menempatkan bangunan di baris paling belakang
            for(int i = 0; i < gameState.gameDetails.mapHeight; i++){
                if(counterBuilding.building[0][i].e == 0 && counterBuilding.building[1][i].a == 0){
                    command = placeBuildingInRowFromBack(BuildingType.ENERGY, i);
                    break;
                }
            }
        }

        return command;
    }

    // fungsi untuk menempatkan bangunan bertipe buildingType di depan baris y
    private String placeBuildingInRowFromFront(BuildingType buildingType, int y) {
        for (int i = (gameState.gameDetails.mapWidth / 2) - 1; i >= 0; i--) {
            // cek sel kosong
            if (isCellEmpty(i, y)) {
                // jika ya, bangun bangunan
                return buildCommand(i, y, buildingType);
            }
            // jika tidak, cek kolom berikutnya
        }
        return "";
    }

    // fungsi untuk menempatkan bangunan bertipe buildingType di belakang garis y
    private String placeBuildingInRowFromBack(BuildingType buildingType, int y) {
        for (int i = 0; i < gameState.gameDetails.mapWidth / 2; i++) {
            // cek sel kosong
            if (isCellEmpty(i, y)) {
                // jika ya, bangun bangunannya
                return buildCommand(i, y, buildingType);
            }
            // jika tidak, cek kolom berikutnya
        }
        return "";
    }

    // fungsi untuk menempatkan bangunan bertipe buildingType di kolom x baris y
    private String buildCommand(int x, int y, BuildingType buildingType) {
        return String.format("%s,%d,%s", String.valueOf(x), y, buildingType.getCommandCode());
    }

    // fungsi yang mengembalikan true apabila sel pada kolom x, baris y kosong
    private boolean isCellEmpty(int x, int y) {
        Optional<CellStateContainer> cellOptional = gameState.getGameMap().stream()
                .filter(c -> c.x == x && c.y == y)
                .findFirst();
        // cek apakah sel yang ditujukan ada di map
        if (cellOptional.isPresent()) {
            CellStateContainer cell = cellOptional.get();
            return cell.getBuildings().size() <= 0;
        } else { // jika tidak ada mengembalikan pesan error
            System.out.println("Invalid cell selected");
        }
        return true;
    }
    // fungsi yang mengembalikan true bila energi cukup untuk membangun bangunan bertipe buildingType
    private boolean canAffordBuilding(BuildingType buildingType) {
        return getEnergy(PlayerType.A) >= getPriceForBuilding(buildingType);
    }
    // fungsi yang mengembalikan besar energi player
    private int getEnergy(PlayerType playerType) {
        return gameState.getPlayers().stream()
                .filter(p -> p.playerType == playerType)
                .mapToInt(p -> p.energy)
                .sum();
    }
    // fungsi yang mengembalikan harga (energi yang dibutuhkan untuk membangun) bangunan bertipe buildingType
    private int getPriceForBuilding(BuildingType buildingType) {
        return gameState.gameDetails.buildingsStats.get(buildingType).price;
    }
}
