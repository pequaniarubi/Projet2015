package fr.univavignon.courbes.physics.groupe07;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import fr.univavignon.courbes.common.Board;
import fr.univavignon.courbes.common.Direction;
import fr.univavignon.courbes.common.Item;
import fr.univavignon.courbes.common.Position;
import fr.univavignon.courbes.common.Snake;
import fr.univavignon.courbes.physics.PhysicsEngine;

public class Round implements PhysicsEngine
{
	public Board ourBoard;
	private double[][] coordSnake;	// donne les coordonnées de la position d'un snake
	private double itemRate = 0.5;
	
	private double ratioItem = 0; // permet de gérer le flux de spawn d'objet, si 0 pas de spawn d'objet, si 1, spawn d'objet
	
	public double getRatioItem() {
		return ratioItem;
	}

	public void setRatioItem(double ratioItem) {
		this.ratioItem = ratioItem;
	}
	
    public double getItemRate() 
    {
         return itemRate; 
    }
    
    public void setItemRate(double itemRate) 
    {  
    	this.itemRate = itemRate; 
    }
    
	public Round(int width, int height, int[] profileIds)
	{
		ourBoard = init(width,height,profileIds);
	}
	
	
	
	/**	
	 * Cette méthode correspond au constructeur de Snake.
	 * <br/>
	 * Les valeurs sont pour le moment fixées arbitrairement.
	 */
	
	public Snake init(Snake snake, int id, Position spawnPosition)
	{
		snake.playerId = id;
		snake.currentX = spawnPosition.x;
		snake.currentY = spawnPosition.y;
		snake.currentAngle = (int)(Math.random() * 359);
		snake.headRadius = 2;
		snake.movingSpeed = 0.5;
		snake.turningSpeed = 1;
		snake.state = true;
		snake.collision = true;
		snake.inversion = false;
		snake.holeRate = 0.05;
		snake.fly = false;
		snake.currentItems = new HashMap<Item, Long>() ;
		snake.currentScore = 0;		// à enlever pour l'IU ?
		
		return snake;
	}
	
	
	
	/**
	 * Cette méthode correspond au constructeur de Board.
	 * <br/>
	 * Il faut créer tous les tableaux nécessaires, c'est-à-dire pour
	 * connaître les positions de tous les snakes et des items.
	 */

	public Board init(int width, int height, int[] profileIds)
	{
		Position posSpawn;
		int playerNbr = profileIds.length;
		coordSnake = new double[playerNbr][2];
	
		ourBoard = new Board();
		ourBoard.width = width;
		ourBoard.height = height;
		ourBoard.snakes = new Snake[playerNbr];	// tableau de snakes
		ourBoard.snakesMap = new HashMap<Position, Integer>();	// pour connaître la position des snakes
		ourBoard.itemsMap = new HashMap<Position, Item>();	// pour connaître la position des items

		for (int i = 0; i < playerNbr ; i++)
		{
			posSpawn = snakeSpawnPos(width, height);
			ourBoard.snakes[i] = new Snake();
			init(ourBoard.snakes[i], profileIds[i] , posSpawn);
			System.out.println("Snake " + Integer.toString(i) + " spawn a la position (" + Integer.toString(posSpawn.x) + ","+ Integer.toString(posSpawn.y) + ")");
		}
		return ourBoard;  
	}
	
	
	
	/**
	 * Cette méthode retourne une position aléatoire où un snake va spawn.
	 * <br/>
	 * Il faut prévoir une marge pour ne pas spawn sur les bords.
	 */
	
	public Position snakeSpawnPos(int width, int height)
	{
		
		Random snake = new Random();
		Position pos = new Position((snake.nextInt((width-20)-20)+ 20), (snake.nextInt((height-20)-20)+ 20));
		return pos;
	}
	
	
	
	/**
	 * Cette méthode créé un Item de façon aléatoire à des coordonnées aléatoires.
	 */
	
	public void itemSpawnPos()
	{
		
		int width = (int) (Math.random() * ourBoard.width);
		int height = (int) (Math.random() * ourBoard.height);
		Position posNewItem = new Position(width,height);
		int nbItems = (int)(Math.random()*Item.values().length);
		Item newItem = Item.values()[nbItems];																			
		ourBoard.itemsMap.put(posNewItem,newItem);

	}
	
	
	
	/**
	 * Cette méthode affecte un item à un ou plusieurs snakes.
	 */
	
	public void addItemToSnake(int id, Item item) {
		switch(item){
			case USER_SPEED:
				ourBoard.snakes[id].currentItems.put(item, (long)item.duration);
				ourBoard.snakes[id].movingSpeed *= 2;
				break;
			case USER_SLOW:
				ourBoard.snakes[id].currentItems.put(item, (long)item.duration);
				ourBoard.snakes[id].movingSpeed *= 0.5;
				break;
			case USER_BIG_HOLE:
				ourBoard.snakes[id].currentItems.put(item, (long)item.duration);
				ourBoard.snakes[id].holeRate *= 0.5;
				break;
			case OTHERS_SPEED:
				for(Snake snake : ourBoard.snakes)
				{
					if (snake.playerId != id)
					{
						snake.currentItems.put(item, (long)item.duration);
						snake.movingSpeed *= 2;
					}
				}
				break;
			case OTHERS_THICK:
				for(Snake snake : ourBoard.snakes)
				{
					if (snake.playerId != id)
					{
						snake.currentItems.put(item, (long)item.duration);
						snake.headRadius *= 2;
					}
				}
				break;
			case OTHERS_SLOW:
				for(Snake snake : ourBoard.snakes)
				{
					if (snake.playerId != id)
					{
						snake.currentItems.put(item, (long)item.duration);
						snake.movingSpeed *= 0.5;
					}
				}
				break;
			case OTHERS_REVERSE:
				for(Snake snake : ourBoard.snakes)
				{
					if (snake.playerId != id) {
						snake.currentItems.put(item, (long)item.duration);
						snake.inversion = true;
					}
				}
				break;
			case COLLECTIVE_THREE_CIRCLES:
					itemRate *= 3;
				break;
			case COLLECTIVE_TRAVERSE_WALL:
				for(Snake snake : ourBoard.snakes)
				{
					snake.currentItems.put(item, (long)item.duration);
					snake.fly = true;
				}
				break;
			case COLLECTIVE_ERASER:
				ourBoard.snakesMap.clear();
				break;
			default:
				System.out.println("Erreur");
				break;
			}
	}
	
	
	
	/**
	 * Cette méthode retire l'effet d'un item à un ou plusieurs snakes.
	 */
	
	public void removeItemToSnake(int id, Item item)
	{
		
		switch(item)
		{
			case USER_SPEED:
				ourBoard.snakes[id].currentItems.remove(item);
				ourBoard.snakes[id].movingSpeed *= 0.5;
				break;
			case USER_SLOW:
				ourBoard.snakes[id].currentItems.remove(item);
				ourBoard.snakes[id].movingSpeed *= 2;
				break;
			case USER_BIG_HOLE:
				ourBoard.snakes[id].currentItems.remove(item);
				ourBoard.snakes[id].holeRate *= 2;
				break;
			case OTHERS_SPEED:
				ourBoard.snakes[id].currentItems.remove(item);
				ourBoard.snakes[id].movingSpeed *= 0.5;
				break;
			case OTHERS_THICK:
				ourBoard.snakes[id].currentItems.remove(item);
				ourBoard.snakes[id].headRadius *= 0.5;
				break;
			case OTHERS_SLOW:
				ourBoard.snakes[id].currentItems.remove(item);
				ourBoard.snakes[id].movingSpeed *= 2;
				break;
			case OTHERS_REVERSE:
				ourBoard.snakes[id].currentItems.remove(item);
				ourBoard.snakes[id].inversion = false;
				break;
			case COLLECTIVE_TRAVERSE_WALL:
				ourBoard.snakes[id].currentItems.remove(item);
				ourBoard.snakes[id].fly = false;
				break;
			default:
				System.out.println("Erreur");
				break;
		}
	}
	
	
	
	/**
	 * Cette méthode retire l'item de la map dès qu'il est récupéré.
	 */
	public void removeItem(Snake snake, Position pos)
	{
		Item itemRecup = ourBoard.itemsMap.get(pos);
		if(itemRecup != null)
		{
			if(snake.state)
			{
				addItemToSnake(snake.playerId, itemRecup);
				ourBoard.itemsMap.remove(pos);
				System.out.println("Snake a rencontré un item");
			}
		}
	}
	
	
	
	/**
	 * Remplit la snakeMap en fonction des coordonnées du snake
	 * 
	 * @param snake 
	 */
	public void fillSnakeHead(Snake snake)
	{
		int id  = snake.playerId;
		int xS  = snake.currentX;
		int yS  = snake.currentY;
		int rad = (int) snake.headRadius;
		Position pos = new Position(0,0);

		// On met la tête dans un carré et on ajoute chaque coordonnée dans 
		// la map si racine_carre((x_point - x_centre)² + (y_centre - y_point)²) < rayonHead
		for(int i = xS - rad; i < xS + rad ; i++)
		{
			for(int j = yS - rad; j < yS + rad ; j++)
			{
				if(Math.sqrt(Math.pow(i - xS, 2) + Math.pow(j - yS, 2)) < rad)
				{
					pos.x = i;
					pos.y = j;
					ourBoard.snakesMap.put(pos, id);
				}
			}
		}
	}
	
	
	
	/**
	 * Cette méthode teste si le snake rentre en contact avec les bords.
	 * <br/>
	 * Il faut prévoir le cas où l'item COLLECTIVE_TRAVERSE_WALL a été utilisé.
	 */
	
	public void deathVsBounds(Snake snake)
	{
		if(snake.currentX < 0 || snake.currentX > ourBoard.width || snake.currentY < 0|| snake.currentY > ourBoard.height) {
			if (!snake.fly)
			{
				snake.state = false; 
				System.out.println("Snake " + snake.playerId + " a touché les bords");
			} 
			else
			{
				if(snake.currentX < 0)
					snake.currentX = ourBoard.width;
				else if(snake.currentX > ourBoard.width)
					snake.currentX = 0;
				if(snake.currentY < 0)
					snake.currentY = ourBoard.height;
				else if(snake.currentY > ourBoard.height)
					snake.currentY = 0;
			}	
		}
	}
	
	
	
	/**
	 * Cette méthode teste si le snake rentre en contact avec la trainée d'un autre snake.
	 */
	
	public void deathVsSnake(Snake snake)
	{

		Position pos = new Position(snake.currentX,snake.currentY);
		
		try
		{
			Integer snakesPos = ourBoard.snakesMap.get(pos);	// position des autres snakes
			
			if(snakesPos == null)
			{
				ourBoard.snakesMap.put(pos,snake.playerId);  
			}
			else
			{
		        if (snakesPos != snake.playerId && snake.collision == true)
		        {
			        snake.state = false;
			        System.out.println("Snake " + snake.playerId + " est mort\nX="+pos.x+"   Y="+pos.y);
		        }
			}
		}catch(NullPointerException e)
		{
			System.out.println("Impossible d'obtenir la position !");
		}
		
	}
	

	
	/**
	 * Cette méthode effectue la mise à jour
	 */
	
	public void update(long elapsedTime, Map<Integer, Direction> commands)
	{
		// Mise à jour des coordonnées des snakes
		updateSnakesPositions(elapsedTime);
		// Mise à jour des directions des snakes
		updateSnakesDirections(elapsedTime, commands);
		// Mise à jour des effets liés aux snakes
		updateSnakesEffects(elapsedTime);
		// Mise à jour du prochain spawn d'item
		updateSpawnItem(elapsedTime);
	}
	

	
	/**
	 * Cette méthode génère l'apparition d'un item pendant la mise à jour
	 */
	
	private void updateSpawnItem(long elapsedTime) {
		ratioItem += elapsedTime*getItemRate();
		if(ratioItem >= 1000) {
			itemSpawnPos();
			ratioItem = 0;
		}
	}
	

	
	/**
	 * Cette méthode affecte un item à un snake.
	 */
	
	public void updateSnakesEffects(long elapsedTime) {

		for(Snake snake : ourBoard.snakes)
		{
			for (Map.Entry<Item, Long> entry : snake.currentItems.entrySet())
			{
				long roundTime = entry.getValue();
				long timeLeft = roundTime - elapsedTime;

				// Enlever l'effet et supprimer l'objet de la liste
				if (timeLeft <= 0 ) {
					removeItemToSnake(snake.playerId, entry.getKey());
				}
				// Mettre à jour le temps restant pour l'effet de l'Item
				else if (timeLeft > 0) {
					snake.currentItems.put(entry.getKey(), timeLeft);
				}
			}
		}
	}
	

	
	/**
	 * Cette méthode effectue la mise à jour de la position du snake.
	 */
	
	public void updateSnakesPositions(long elapsedTime) {
		long elapsed;
		double pixStep;
		boolean snakeMove = false;
		Position pos = new Position(0,0);
		for(Snake snake : ourBoard.snakes)
		{

			elapsed = elapsedTime;
			pixStep = 0;
			while (elapsed > 0 && snake.state == true)
			{

				/** Gestion de la future position du snake en fonction de son angle **/
				while(pixStep < 1 && elapsed > 0) {
					elapsed--;
					pixStep += snake.movingSpeed;
				}
				coordSnake[snake.playerId][0] += Math.cos(Math.toRadians(snake.currentAngle));
				coordSnake[snake.playerId][1] += Math.sin(Math.toRadians(snake.currentAngle));

				if(coordSnake[snake.playerId][1] >= 1 && coordSnake[snake.playerId][0] >= 1) {
					snake.currentY--;
					snake.currentX++;
					pos.x = snake.currentX;
					pos.y = snake.currentY;
					ourBoard.snakesMap.put(pos , snake.playerId);
					coordSnake[snake.playerId][1]--;
					coordSnake[snake.playerId][0]--;
					snakeMove = true;
				}
				else if(coordSnake[snake.playerId][1] <= -1 && coordSnake[snake.playerId][0] >= 1) {
					snake.currentY++;
					snake.currentX++;
					pos.x = snake.currentX;
					pos.y = snake.currentY;
					ourBoard.snakesMap.put(pos , snake.playerId);
					coordSnake[snake.playerId][1]++;
					coordSnake[snake.playerId][0]--;
					snakeMove = true;
				}
				else if(coordSnake[snake.playerId][1] <= -1 && coordSnake[snake.playerId][0] <= -1) {
					snake.currentY++;
					snake.currentX--;
					pos.x = snake.currentX;
					pos.y = snake.currentY;
					ourBoard.snakesMap.put(pos , snake.playerId);
					coordSnake[snake.playerId][1]++;
					coordSnake[snake.playerId][0]++;
					snakeMove = true;
				}
				else if(coordSnake[snake.playerId][1] >= 1 && coordSnake[snake.playerId][0] <= -1) {
					snake.currentY--;
					snake.currentX--;
					pos.x = snake.currentX;
					pos.y = snake.currentY;
					ourBoard.snakesMap.put(pos , snake.playerId);
					coordSnake[snake.playerId][1]--;
					coordSnake[snake.playerId][0]++;
					snakeMove = true;
				}
				else if(coordSnake[snake.playerId][1] >= 1) {
					snake.currentY--;
					pos.x = snake.currentX;
					pos.y = snake.currentY;
					ourBoard.snakesMap.put(pos , snake.playerId);
					coordSnake[snake.playerId][1]--;
					snakeMove = true;
				}
				else if(coordSnake[snake.playerId][1] <= -1) {
					snake.currentY++;
					pos.x = snake.currentX;
					pos.y = snake.currentY;
					ourBoard.snakesMap.put(pos , snake.playerId);
					coordSnake[snake.playerId][1]++;
					snakeMove = true;
				}
				else if(coordSnake[snake.playerId][0] >= 1) {
					snake.currentX++;
					pos.x = snake.currentX;
					pos.y = snake.currentY;
					ourBoard.snakesMap.put(pos , snake.playerId);
					coordSnake[snake.playerId][0]--;
					snakeMove = true;
				}
				else if(coordSnake[snake.playerId][0] <= -1) {
					snake.currentX--;
					pos.x = snake.currentX;
					pos.y = snake.currentY;
					ourBoard.snakesMap.put(pos , snake.playerId);
					coordSnake[snake.playerId][0]++;
					snakeMove = true;
				}

				pixStep --;
				System.out.println("Position snake "+ Integer.toString(snake.playerId)+ " x:" + Integer.toString(snake.currentX) + " y:" + Integer.toString(snake.currentY));

				if(snakeMove) {
					fillSnakeHead(snake);
					deathVsBounds(snake);
					deathVsSnake(snake);
					removeItem(snake,pos);
				}
				
			}
		}
	}
	

	
	/**
	 * Cette méthode effectue la mise à jour de la direction du snake.
	 */
	
	public void updateSnakesDirections(long elapsedTime, Map<Integer, Direction> commands)
	{
		Direction direction;
		for(Snake snake : ourBoard.snakes)
		{
			direction = commands.get(snake.playerId);
			if(direction != null)
			{
				switch (direction)
				{
				case LEFT:
					if(snake.inversion == false)
						snake.currentAngle += elapsedTime*Math.toDegrees(snake.turningSpeed);
					else
						snake.currentAngle -= elapsedTime*Math.toDegrees(snake.turningSpeed);
					break;
				case RIGHT:
					if(snake.inversion == false)
						snake.currentAngle -= elapsedTime*Math.toDegrees(snake.turningSpeed);
					else
						snake.currentAngle += elapsedTime*Math.toDegrees(snake.turningSpeed);
					break;
				case NONE:
					break;
				default:
					break;
				}
			}
		}
	}
	
	

	/**
	 * Cette méthode écrase le tableau actuel.
	 */
	
	public void forceUpdate(Board board)
	{
		this.ourBoard = board;
	}
	
	
	
}