package org.skiBums;

import org.junit.jupiter.api.Test;
import org.skiBums.Tiles.LandTile;
import org.skiBums.Tiles.Tile;
import org.skiBums.geometry.HexCoord;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TileAndBoardSetupTest {

    @Test
    void setUpTilesProducesOneTilePerLandHex() {
        Map<HexCoord, Tile> map = Tile.setUpTiles();
        assertEquals(GameConstants.TILES_AMOUNT, map.size());
        assertEquals(GameConstants.ALL_LAND_HEXES, map.keySet());
        for (Map.Entry<HexCoord, Tile> e : map.entrySet()) {
            assertInstanceOf(LandTile.class, e.getValue());
            assertEquals(e.getKey(), e.getValue().getHexCoord());
        }
    }

    @Test
    void boardSetupRuns() {
        Board board = new Board();
        board.setup();
        assertEquals(GameConstants.TILES_AMOUNT, board.getLandTiles().size());
        assertTrue(board.tileAt(new HexCoord(0, 0)).isPresent());
    }
}
