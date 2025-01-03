import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * L'agent Atelier est responsable de la coordination des produits et des robots.
 * Il attribue des tâches aux robots, gère la liste des produits à traiter
 * et suit les produits terminés en incrémentant un compteur.
 * 
 * Fonctionnalités principales :
 * - Attribuer des produits aux robots.
 * - Recevoir les mises à jour sur les produits terminés.
 * - Compter le nombre de messages échangés.
 * 
 */
public class AtelierAgent extends Agent {
    private List<Product> products = new ArrayList<>();
    private int completedProducts = 0; // Produits terminés
    private int totalMessages = 0;     // Messages échangés

    /**
     * Initialisation de l'agent Atelier.
     * Ajoute des produits pour les tests et configure le comportement cyclique.
     */
    @Override
    protected void setup() {
        System.out.println("Atelier initialisé.");

        // Ajout de produits pour les tests
        //Il est possible d'ajouter/retirer autant de produits que nécessaire pour les tests
        products.add(new Product("Produit1", 3));
        products.add(new Product("Produit2", 3));
        products.add(new Product("Produit3", 3));
        products.add(new Product("Produit4", 3));
        products.add(new Product("Produit5", 3));

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    totalMessages++; // Compteur des messages
        
                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        System.out.println(msg.getSender().getLocalName() + " informe de ses compétences.");
                        assignProductToRobot(msg.getSender().getLocalName());
                    } else if (msg.getPerformative() == ACLMessage.CONFIRM) {
                        completedProducts++; // Incrément des produits terminés
                        System.out.println("Produit terminé ! Total actuel : " + completedProducts);
                    }
                } else {
                    block();
                }
            }
        });        
    }

    /**
     * Attribue un produit à un robot.
     * 
     * @param robotName Nom du robot à qui le produit sera attribué.
     */
    private void assignProductToRobot(String robotName) {
        if (!products.isEmpty()) {
            Product product = products.remove(0);
            System.out.println("Atelier attribue " + product.getName() + " à " + robotName);

            ACLMessage assignMsg = new ACLMessage(ACLMessage.REQUEST);
            assignMsg.addReceiver(getAID(robotName));
            assignMsg.setContent(product.serialize());
            send(assignMsg);

            totalMessages++;
        }
    }

    /**
     * Actions effectuées lorsque l'agent Atelier est arrêté.
     * Affiche les statistiques finales : nombre de produits réalisés et messages échangés.
     */
    @Override
    protected void takeDown() {
        System.out.println("Test terminé !");
        System.out.println("Produits réalisés : " + completedProducts);
        System.out.println("Messages échangés : " + totalMessages);
    }
}
