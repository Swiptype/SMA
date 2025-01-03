import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Classe représentant un produit dans l'atelier.
 * 
 * Un produit est caractérisé par :
 * - Un nom.
 * - Une liste de compétences nécessaires sous forme d'une HashMap (compétence → état : true/false).
 * 
 * Fonctionnalités principales :
 * - Suivre les compétences appliquées.
 * - Sérialiser et désérialiser l'objet pour le passage entre agents.
 * - Vérifier si toutes les compétences nécessaires sont appliquées.
 * 
 */
public class Product implements Serializable {
    private String name;
    private HashMap<String, Boolean> requiredSkills = new HashMap<>();

    /**
     * Constructeur de produit.
     * 
     * @param name Nom du produit.
     * @param totalSkills Nombre total de compétences nécessaires.
     */
    public Product(String name, int totalSkills) {
        this.name = name;
        for (int i = 1; i <= totalSkills; i++) {
            requiredSkills.put("skill" + i, false); // Compétences nécessaires initialisées à false
        }
    }

    /**
     * Récupère le nom du produit.
     * 
     * @return Nom du produit.
     */
    public String getName() {
        return name;
    }

    /**
     * Récupère la liste des compétences pas encore appliquées.
     * 
     * @return Liste des compétences restantes.
     */
    public List<String> getRequiredSkills() {
        List<String> skills = new ArrayList<>();
        for (String skill : requiredSkills.keySet()) {
            if (!requiredSkills.get(skill)) {
                skills.add(skill);
            }
        }
        return skills;
    }

    /**
     * Applique une compétence au produit.
     * 
     * @param skill Compétence à appliquer.
     */
    public void applySkill(String skill) {
        if (requiredSkills.containsKey(skill)) {
            requiredSkills.put(skill, true); // Marquer la compétence comme appliquée
            System.out.println("Compétence " + skill + " appliquée avec succès au produit " + name);
        }
    }
    
    /**
     * Vérifie si le produit est entièrement traité.
     * 
     * @return true si toutes les compétences sont appliquées, sinon renvoie false.
     */
    public boolean isComplete() {
        return !requiredSkills.containsValue(false); // Retourne true si toutes les compétences sont "true"
    }       

    /**
     * Sérialise le produit sous forme de chaîne.
     * 
     * @return Chaîne représentant l'état du produit.
     */
    public String serialize() {
        return name + "," + requiredSkills.toString();
    }

    /**
     * Désérialise une chaîne pour reconstruire un objet produit.
     * 
     * @param data Chaîne sérialisée représentant un produit.
     * @return Produit reconstruit.
     */
    public static Product deserialize(String data) {
        String[] parts = data.split(",", 2);
        String name = parts[0];
        String skillsStr = parts[1].replace("{", "").replace("}", "");
        HashMap<String, Boolean> skills = new HashMap<>();
        for (String pair : skillsStr.split(", ")) {
            String[] keyValue = pair.split("=");
            skills.put(keyValue[0], Boolean.parseBoolean(keyValue[1]));
        }
        Product product = new Product(name, skills.size());
        product.requiredSkills = skills; // Récupérer les compétences exactes
        System.out.println("Produit " + name + " désérialisé avec compétences: " + skills);
        return product;
    }
}
