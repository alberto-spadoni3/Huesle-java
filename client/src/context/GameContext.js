import { Outlet } from "react-router-dom";
import { GameDataProvider } from "./GameDataProvider";

const GameContext = () => {
    return (
        <GameDataProvider>
            <Outlet />
        </GameDataProvider>
    );
};

export default GameContext;
