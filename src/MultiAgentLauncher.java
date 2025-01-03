import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.util.Scanner;

/**
 * Classe de lancement du système multi-agent.
 * 
 * Permet de configurer dynamiquement le nombre de robots, le nombre de compétences,
 * ainsi que les paramètres temporels pour les tests.
 * 
 * Fonctionnalités principales :
 * - Initialiser l'agent Atelier.
 * - Créer et configurer dynamiquement les agents Robots.
 * - Permettre des paramètres d'exécution flexibles.
 * 
 */
public class MultiAgentLauncher {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Configuration dynamique par l'utilisateur
        System.out.print("Nombre d'agents robots : ");
        int robotCount = scanner.nextInt();

        System.out.print("Nombre de compétences distinctes : ");
        int skillCount = scanner.nextInt();

        System.out.print("Temps λ1 (arrivée des produits) : ");
        int lambda1 = scanner.nextInt();

        System.out.print("Temps λ2 (intervalle de production) : ");
        int lambda2 = scanner.nextInt();

        System.out.print("Temps λ3 (temps de traitement par robot) : ");
        int lambda3 = scanner.nextInt();

        try {
            // Initialisation de l'environnement JADE
            Runtime runtime = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            AgentContainer container = runtime.createMainContainer(profile);

            // Création de l'agent Atelier
            AgentController atelier = container.createNewAgent("Atelier", AtelierAgent.class.getName(), null);
            atelier.start();

            // Création des robots
            for (int i = 1; i <= robotCount; i++) {
                Object[] robotArgs = {skillCount};
                AgentController robot = container.createNewAgent("Robot" + i, RobotAgent.class.getName(), robotArgs);
                robot.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
