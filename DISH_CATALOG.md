# SlimTrack 3.4.0 — prepared dishes

The catalogue includes 325 representative recipes across ten cuisine groups: 172 Ukrainian and 34 Transcarpathian dishes, plus international, Italian, Georgian, Asian, Caucasian, European, Mediterranean and Central Asian dishes. It includes 107 reference ingredients.

Both the diary picker and catalogue open in Dishes mode. Products and All remain available. Category and cuisine filters combine with multilingual name/alias search. A search in the catalogue no longer restricts the diary's available foods.

Calories and macros are calculated from each recipe's ingredient quantities and finished edible weight. These are internal reference estimates, not laboratory measurements of a specific cooked dish. The UI marks standard recipes as estimates. Cooked ingredient states, retained oil and representative finished weights are explicit in the source asset.

My recipe accepts ingredient weights and the actual finished dish weight, then saves a custom dish with nutrition per 100 g. Portions in the diary scale the whole dish's nutrition. The recipe calculator rejects missing, non-finite, negative and impossible weights. For ingredients not in the catalogue, users can first enter their label values in My product.

The built-in catalogue is bundled with the APK and works offline. It is merged with saved foods without rewriting Room or modifying diary, profile or barcode data. Custom dishes are retained by Room. The standard catalogue is finite and extensible; it does not claim to contain every possible recipe.

Source: `overrides/assets/prepared_dishes.json`. To regenerate, run `python3 catalog-source/generate.py` from the repository root. Ingredient reference values are explicitly labelled internal estimates. The linked USDA methodology reference explains the importance of finished yield; it is not claimed as provenance for every reference value.

Validation: recipe calculation tests, catalogue completeness and multilingual/alias search tests, Room preservation/search tests, existing barcode regression tests, and Android APK assembly/signature verification in GitHub Actions.
