import json
from pathlib import Path
# Representative reference values, not laboratory measurements of a particular recipe.
# Ingredient weights below refer to edible components; cooked states are explicit.
rows = '''
water|Вода|Вода|Water|0|0|0|0
broth|Бульон овощной|Овочевий бульйон|Vegetable broth|8|0.3|0.1|1.4
chicken|Курица отварная без кожи|Курка відварена без шкіри|Cooked skinless chicken|165|31|3.6|0
beef|Говядина тушеная|Яловичина тушкована|Cooked beef|210|28|10|0
pork|Свинина приготовленная|Свинина приготовлена|Cooked pork|270|26|18|0
lamb|Баранина приготовленная|Баранина приготовлена|Cooked lamb|258|25|17|0
mince|Фарш мясной приготовленный|Фарш м’ясний приготовлений|Cooked mixed ground meat|250|25|17|0
liver|Печень приготовленная|Печінка приготовлена|Cooked liver|175|26|6|4
turkey|Индейка приготовленная|Індичка приготовлена|Cooked turkey|150|29|3.5|0
duck|Утка приготовленная с кожей|Качка приготовлена зі шкірою|Cooked duck with skin|337|19|28|0
bacon|Бекон приготовленный|Бекон приготовлений|Cooked bacon|541|37|42|1.4
sausage|Колбаса вареная|Ковбаса варена|Cooked sausage|260|13|22|2
ham|Ветчина|Шинка|Ham|145|21|6|1
whitefish|Белая рыба приготовленная|Біла риба приготовлена|Cooked white fish|100|22|1|0
salmon|Лосось приготовленный|Лосось приготовлений|Cooked salmon|206|22|13|0
herring|Сельдь соленая|Оселедець солоний|Salted herring|217|20|15|0
tuna|Тунец консервированный без масла|Тунець консервований без олії|Canned tuna in water|116|26|1|0
shrimp|Креветки приготовленные|Креветки приготовлені|Cooked shrimp|99|24|0.3|0.2
mackerel|Скумбрия приготовленная|Скумбрія приготовлена|Cooked mackerel|262|24|18|0
carp|Карп приготовленный|Короп приготовлений|Cooked carp|162|23|7|0
pikeperch|Судак приготовленный|Судак приготовлений|Cooked zander|100|23|1|0
squid|Кальмар приготовленный|Кальмар приготовлений|Cooked squid|110|20|2|3
mussels|Мидии приготовленные|Мідії приготовлені|Cooked mussels|172|24|4.5|7
crab|Крабовые палочки|Крабові палички|Surimi crab sticks|95|7|1|15
potato|Картофель вареный без масла|Картопля варена без олії|Boiled potatoes without oil|87|1.9|0.1|20
rice|Рис вареный без масла|Рис варений без олії|Cooked rice without oil|130|2.7|0.3|28
buckwheat|Гречка вареная без масла|Гречка варена без олії|Cooked buckwheat without oil|110|4.2|1.3|21.3
oats|Овсяная каша на воде|Вівсяна каша на воді|Oat porridge with water|88|3|1.7|15
millet|Пшено вареное|Пшоно варене|Cooked millet|119|3.5|1|24
barley|Перловка вареная|Перлова крупа варена|Cooked pearl barley|123|2.3|0.4|28
cornmeal|Кукурузная каша на воде|Кукурудзяна каша на воді|Cooked cornmeal porridge|75|1.5|0.5|16
couscous|Кускус приготовленный|Кускус приготовлений|Cooked couscous|112|3.8|0.2|23
bulgur|Булгур приготовленный|Булгур приготовлений|Cooked bulgur|83|3.1|0.2|19
quinoa|Киноа приготовленная|Кіноа приготовлена|Cooked quinoa|120|4.4|1.9|21
pasta|Макароны вареные без масла|Макарони варені без олії|Cooked pasta without oil|150|5.5|0.8|30
noodles|Лапша приготовленная|Локшина приготовлена|Cooked noodles|138|4.5|2|25
beans|Фасоль вареная|Квасоля варена|Cooked beans|127|8.7|0.5|23
peas|Горох вареный|Горох варений|Cooked split peas|118|8.3|0.4|21
lentils|Чечевица вареная|Сочевиця варена|Cooked lentils|116|9|0.4|20
chickpeas|Нут вареный|Нут варений|Cooked chickpeas|164|8.9|2.6|27
beet|Свекла вареная|Буряк варений|Cooked beetroot|44|1.7|0.2|10
cabbage|Капуста белокочанная|Капуста білокачанна|White cabbage|25|1.3|0.1|6
sauerkraut|Капуста квашеная|Капуста квашена|Sauerkraut|19|0.9|0.1|4.3
carrot|Морковь|Морква|Carrot|41|0.9|0.2|10
onion|Лук репчатый|Цибуля ріпчаста|Onion|40|1.1|0.1|9.3
garlic|Чеснок|Часник|Garlic|149|6.4|0.5|33
tomato|Помидоры|Помідори|Tomatoes|18|0.9|0.2|3.9
paste|Томатная паста|Томатна паста|Tomato paste|82|4.3|0.5|19
pepper|Перец сладкий|Перець солодкий|Sweet pepper|31|1|0.3|6
cucumber|Огурец|Огірок|Cucumber|15|0.7|0.1|3.6
pickle|Огурец соленый|Огірок солоний|Pickled cucumber|12|0.5|0.1|2.4
zucchini|Кабачок|Кабачок|Zucchini|17|1.2|0.3|3.1
eggplant|Баклажан|Баклажан|Eggplant|25|1|0.2|6
pumpkin|Тыква|Гарбуз|Pumpkin|26|1|0.1|6.5
broccoli|Брокколи приготовленная|Броколі приготовлена|Cooked broccoli|35|2.4|0.4|7
cauliflower|Цветная капуста приготовленная|Цвітна капуста приготовлена|Cooked cauliflower|23|1.8|0.5|4
mushroom|Грибы приготовленные без масла|Гриби приготовлені без олії|Cooked mushrooms without oil|28|3.6|0.5|4
spinach|Шпинат|Шпинат|Spinach|23|2.9|0.4|3.6
lettuce|Листовой салат|Листовий салат|Leaf lettuce|15|1.4|0.2|2.9
corn|Кукуруза консервированная|Кукурудза консервована|Canned corn|86|3.2|1.2|19
olives|Оливки|Оливки|Olives|145|1|15|4
avocado|Авокадо|Авокадо|Avocado|160|2|15|9
apple|Яблоко|Яблуко|Apple|52|0.3|0.2|14
banana|Банан|Банан|Banana|89|1.1|0.3|23
berries|Ягоды|Ягоди|Mixed berries|45|0.7|0.3|10
cherry|Вишня без косточек|Вишня без кісточок|Pitted sour cherries|50|1|0.3|12
plum|Слива|Слива|Plum|46|0.7|0.3|11
apricot|Абрикос|Абрикос|Apricot|48|1.4|0.4|11
lemon|Лимон|Лимон|Lemon|29|1.1|0.3|9
raisins|Изюм|Родзинки|Raisins|299|3.1|0.5|79
prunes|Чернослив|Чорнослив|Prunes|240|2.2|0.4|64
egg|Яйцо без скорлупы|Яйце без шкаралупи|Egg without shell|143|12.6|9.5|0.7
milk|Молоко 2.5%|Молоко 2.5%|Milk 2.5%|52|3|2.5|4.7
curd|Творог 5%|Кисломолочний сир 5%|Cottage cheese 5%|121|17|5|3
yogurt|Йогурт натуральный 2%|Йогурт натуральний 2%|Plain yogurt 2%|73|10|2|3.6
kefir|Кефир 2.5%|Кефір 2.5%|Kefir 2.5%|53|3|2.5|4
cream|Сливки 20%|Вершки 20%|Cream 20%|206|2.5|20|3.7
sourcream|Сметана 15%|Сметана 15%|Sour cream 15%|160|2.6|15|3.6
cheese|Сыр твердый|Сир твердий|Hard cheese|350|25|27|2
feta|Брынза|Бринза|Brined white cheese|265|14|21|4
mozzarella|Моцарелла|Моцарела|Mozzarella|280|22|21|2
butter|Масло сливочное|Масло вершкове|Butter|748|0.5|82|0.8
oil|Масло растительное|Олія рослинна|Vegetable oil|884|0|100|0
lard|Сало|Сало|Pork fat|797|2.4|89|0
flour|Мука пшеничная|Борошно пшеничне|Wheat flour|364|10|1|76
semolina|Манная крупа сухая|Манна крупа суха|Dry semolina|360|12.7|1|73
starch|Крахмал|Крохмаль|Starch|350|0.2|0.1|86
bread|Хлеб пшеничный|Хліб пшеничний|Wheat bread|250|8|3|49
ryebread|Хлеб ржаной|Хліб житній|Rye bread|230|7|1.5|46
breadcrumbs|Панировочные сухари|Панірувальні сухарі|Breadcrumbs|395|13|5|72
lavash|Лаваш тонкий|Лаваш тонкий|Thin flatbread|277|9|1|57
tortilla|Тортилья|Тортилья|Tortilla|310|8|8|52
sugar|Сахар|Цукор|Sugar|400|0|0|100
honey|Мед|Мед|Honey|304|0.3|0|82
jam|Варенье|Варення|Jam|250|0.3|0.1|65
chocolate|Шоколад темный|Шоколад темний|Dark chocolate|550|6|35|55
cocoa|Какао порошок|Какао порошок|Cocoa powder|228|20|14|58
walnuts|Грецкие орехи|Волоські горіхи|Walnuts|654|15|65|14
almonds|Миндаль|Мигдаль|Almonds|579|21|50|22
peanut|Арахис|Арахіс|Peanuts|567|26|49|16
sesame|Кунжут|Кунжут|Sesame seeds|573|18|50|23
poppy|Мак|Мак|Poppy seeds|525|18|42|28
mayo|Майонез|Майонез|Mayonnaise|680|1|75|1
mustard|Горчица|Гірчиця|Mustard|66|4|4|5
soy|Соевый соус|Соєвий соус|Soy sauce|53|8|0|5
coconut|Кокосовое молоко|Кокосове молоко|Coconut milk|190|2|19|3
tofu|Тофу|Тофу|Tofu|120|13|7|2
'''
ingredients=[]
for line in rows.strip().splitlines():
    key,ru,uk,en,*nums=line.split('|')
    ingredients.append(dict(id=key,names=dict(ru=ru,uk=uk,en=en),kcal=float(nums[0]),protein=float(nums[1]),fat=float(nums[2]),carbs=float(nums[3])))
byid={i['id']:i for i in ingredients}
dishes=[]
def add(ru,uk,en,category,cuisine,recipe,portion=250,aliases='',yield_g=None):
    components=[]
    for pair in recipe.split():
        key,grams=pair.split(':'); assert key in byid,key
        components.append(dict(ingredient=key,grams=float(grams)))
    finished=yield_g or sum(c['grams'] for c in components)
    dishes.append(dict(id='dish_'+str(len(dishes)+1).zfill(4),names=dict(ru=ru,uk=uk,en=en),category=category,cuisine=cuisine,serving_g=portion,finished_weight_g=finished,ingredients=components,aliases=aliases.split(',') if aliases else []))
def group(category,cuisine,lines,portion=250):
    for line in lines.strip().splitlines():
        if not line.strip():continue
        parts=line.split('|'); assert len(parts)>=4,line
        add(*parts[:3],category,cuisine,parts[3],portion,parts[4] if len(parts)>4 else '')
group('Супы','Украинская','''
Борщ украинский с говядиной|Борщ український з яловичиною|Ukrainian beef borscht|beef:250 beet:350 cabbage:300 potato:400 carrot:100 onion:100 paste:70 oil:20 broth:1600|борщ,борщ український,borsh,borscht
Борщ со свининой|Борщ зі свининою|Pork borscht|pork:250 beet:350 cabbage:300 potato:400 carrot:100 onion:100 paste:70 oil:20 broth:1600
Борщ с курицей|Борщ з куркою|Chicken borscht|chicken:250 beet:350 cabbage:300 potato:400 carrot:100 onion:100 paste:70 oil:20 broth:1600
Борщ постный с фасолью|Борщ пісний з квасолею|Bean borscht|beans:300 beet:350 cabbage:300 potato:400 carrot:100 onion:100 paste:70 oil:20 broth:1600
Борщ зеленый со шпинатом|Борщ зелений зі шпинатом|Green borscht with spinach|chicken:200 spinach:350 potato:400 egg:150 carrot:100 onion:100 broth:1600 sourcream:80
Борщ зеленый без мяса|Борщ зелений без м’яса|Vegetarian green borscht|spinach:350 potato:400 egg:150 carrot:100 onion:100 broth:1800 sourcream:80
Борщ с черносливом|Борщ із чорносливом|Borscht with prunes|beet:400 cabbage:300 potato:400 prunes:100 beans:200 paste:70 onion:100 oil:20 broth:1600
Капустняк со свининой|Капусняк зі свининою|Pork sauerkraut soup|pork:250 sauerkraut:450 millet:250 potato:300 carrot:100 onion:100 broth:1500
Капустняк постный|Капусняк пісний|Vegetarian sauerkraut soup|sauerkraut:500 millet:300 potato:400 carrot:100 onion:100 oil:20 broth:1600
Кулеш с пшеном и салом|Куліш із пшоном та салом|Millet and pork-fat soup|millet:600 potato:300 onion:150 lard:70 broth:1000|кулеш,куліш,kulish
Куриный суп с домашней лапшой|Курячий суп із домашньою локшиною|Chicken noodle soup|chicken:250 noodles:400 carrot:150 onion:100 broth:1600
Суп с галушками|Суп із галушками|Ukrainian dumpling soup|flour:150 egg:50 milk:70 chicken:180 potato:250 carrot:100 onion:100 broth:1600
Юшка рыбная|Юшка рибна|Ukrainian fish soup|whitefish:350 potato:400 carrot:150 onion:150 broth:1600|уха,юшка,fish soup
Гороховый суп с копченостями|Гороховий суп із копченостями|Pea soup with smoked meat|peas:600 ham:250 potato:300 carrot:100 onion:100 broth:1300
Гороховый суп постный|Гороховий суп пісний|Vegetarian pea soup|peas:700 potato:300 carrot:100 onion:100 oil:20 broth:1500
Суп фасолевый|Суп квасолевий|Bean soup|beans:600 potato:350 carrot:100 onion:100 paste:70 oil:20 broth:1500
Суп грибной с перловкой|Суп грибний з перловкою|Mushroom barley soup|mushroom:450 barley:400 potato:300 carrot:100 onion:100 oil:20 broth:1500
Суп картофельный|Суп картопляний|Potato soup|potato:650 carrot:150 onion:100 butter:30 broth:1600
Рассольник с перловкой|Розсольник з перловкою|Pickle and barley soup|beef:200 barley:350 pickle:350 potato:350 carrot:100 onion:100 broth:1600
Солянка мясная|Солянка м’ясна|Mixed meat solyanka|beef:200 sausage:200 ham:150 pickle:250 olives:80 onion:150 paste:80 broth:1500
Свекольник холодный|Холодник буряковий|Cold beet soup|beet:600 cucumber:300 egg:200 kefir:1200 water:500|свекольник,холодник
Суп с фрикадельками|Суп із фрикадельками|Meatball soup|beef:150 pork:150 rice:100 potato:400 carrot:100 onion:100 broth:1600
''',350)
group('Супы','Закарпатская','''
Бограч закарпатский|Бограч закарпатський|Transcarpathian bograch|beef:500 pork:300 potato:600 pepper:250 tomato:250 onion:200 lard:40 broth:1100|бограч,bogracs,bograch
Боб-гуляш|Боб-гуляш|Transcarpathian bean goulash|beans:600 pork:300 ham:150 pepper:150 onion:150 tomato:200 broth:1000|боб гуляш,babgulyas
Грибная юшка закарпатская|Грибна юшка закарпатська|Transcarpathian mushroom soup|mushroom:500 sourcream:200 flour:30 onion:100 broth:1500
Левеш куриный|Левеш курячий|Transcarpathian chicken leves|chicken:350 noodles:350 carrot:200 onion:100 broth:1700|левеш,leves
Кромпли-левеш|Крумплі-левеш|Transcarpathian potato leves|potato:700 sourcream:150 flour:30 onion:120 broth:1400|кромпли,крумплі
Гуляш-левеш|Гуляш-левеш|Goulash soup|beef:450 potato:450 pepper:150 tomato:200 onion:180 broth:1200
''',350)
group('Супы','Международная','''
Крем-суп грибной|Крем-суп грибний|Cream of mushroom soup|mushroom:700 potato:300 onion:150 cream:250 broth:900
Крем-суп тыквенный|Крем-суп гарбузовий|Pumpkin cream soup|pumpkin:800 potato:250 onion:100 cream:150 broth:850
Крем-суп из брокколи|Крем-суп із броколі|Broccoli cream soup|broccoli:700 potato:200 cream:200 broth:900
Крем-суп из цветной капусты|Крем-суп із цвітної капусти|Cauliflower cream soup|cauliflower:800 potato:200 cream:150 broth:900
Сырный суп с курицей|Сирний суп із куркою|Chicken cheese soup|chicken:250 cheese:200 potato:400 carrot:100 onion:100 broth:1500
Суп томатный|Суп томатний|Tomato soup|tomato:1000 paste:100 onion:150 oil:30 broth:700
Суп чечевичный|Суп сочевичний|Lentil soup|lentils:800 carrot:150 onion:150 paste:80 oil:30 broth:1000
Суп с рисом и овощами|Суп із рисом та овочами|Rice and vegetable soup|rice:500 potato:300 carrot:150 onion:100 pepper:150 broth:1400
Суп с кабачком|Суп із кабачком|Zucchini soup|zucchini:700 potato:300 carrot:150 onion:100 oil:20 broth:1400
Суп-пюре гороховый|Суп-пюре гороховий|Split pea puree soup|peas:1000 onion:150 carrot:150 butter:30 broth:900
''',350)
group('Супы','Итальянская','''
Минестроне|Мінестроне|Minestrone|beans:250 pasta:250 tomato:300 zucchini:300 carrot:150 onion:100 broth:1300 oil:25
''',350)
group('Супы','Грузинская','''
Харчо с говядиной|Харчо з яловичиною|Beef kharcho|beef:350 rice:300 tomato:300 paste:80 onion:200 walnuts:70 broth:1400
''',350)
group('Супы','Азиатская','''
Том-ям с креветками|Том-ям із креветками|Tom yum shrimp soup|shrimp:350 mushroom:300 tomato:200 coconut:300 broth:1100 lemon:40
Рамен с курицей|Рамен із куркою|Chicken ramen|chicken:300 noodles:600 egg:200 mushroom:150 soy:50 broth:1400
Мисо-суп с тофу (упрощенный)|Місо-суп із тофу (спрощений)|Simplified tofu miso-style soup|tofu:350 soy:70 mushroom:150 broth:1400
''',350)
group('Мясные блюда','Украинская','''
Котлеты домашние жареные|Котлети домашні смажені|Fried homemade meat patties|mince:600 bread:150 egg:100 onion:150 oil:45|котлеты,котлети,cutlets
Котлеты куриные жареные|Котлети курячі смажені|Fried chicken patties|chicken:600 bread:150 egg:100 onion:150 oil:40
Котлеты куриные на пару|Котлети курячі на парі|Steamed chicken patties|chicken:600 bread:100 egg:100 onion:150
Котлеты из индейки|Котлети з індички|Turkey patties|turkey:600 bread:120 egg:100 onion:150 oil:25
Биточки мясные в томатном соусе|Биточки м’ясні в томатному соусі|Meat patties in tomato sauce|mince:600 bread:120 egg:100 onion:150 tomato:300 paste:70 broth:200 oil:25|битки,биточки,биток,битки м'ясні
Биточки куриные|Биточки курячі|Chicken bitky|chicken:600 bread:120 egg:100 onion:120 oil:30
Тефтели с рисом|Тефтелі з рисом|Meatballs with rice|mince:500 rice:350 egg:100 onion:150 tomato:300 oil:20
Фрикадельки в сливочном соусе|Фрикадельки у вершковому соусі|Meatballs in cream sauce|mince:600 egg:100 onion:150 cream:250 broth:150
Котлета по-киевски|Котлета по-київськи|Chicken Kyiv|chicken:650 butter:100 breadcrumbs:180 egg:150 oil:50|киевская котлета,chicken kiev
Котлеты свиные|Котлети свинячі|Pork patties|pork:650 bread:150 egg:100 onion:150 oil:35
Отбивная свиная в панировке|Відбивна свиняча в паніруванні|Breaded pork chop|pork:700 breadcrumbs:120 egg:100 oil:40|отбивные,відбивні
Отбивная куриная|Відбивна куряча|Breaded chicken cutlet|chicken:700 breadcrumbs:120 egg:100 oil:35
Отбивная из индейки|Відбивна з індички|Breaded turkey cutlet|turkey:700 breadcrumbs:100 egg:100 oil:30
Крученики с грибами|Крученики з грибами|Meat rolls with mushrooms|pork:700 mushroom:300 onion:150 sourcream:150 oil:25|крученики,kruchenyky
Крученики с салом|Крученики із салом|Pork rolls with pork fat|pork:750 lard:100 onion:150 oil:20
Голубцы с мясом и рисом|Голубці з м’ясом та рисом|Cabbage rolls with meat and rice|cabbage:700 mince:400 rice:450 onion:150 carrot:150 paste:70 oil:25 broth:250|голубцы,голубці,holubtsi
Ленивые голубцы|Ліниві голубці|Lazy cabbage rolls|cabbage:600 mince:450 rice:400 onion:150 paste:70 egg:100 oil:25 broth:200
Перец фаршированный мясом|Перець фарширований м’ясом|Meat-stuffed peppers|pepper:700 mince:450 rice:400 onion:150 paste:70 oil:25 broth:200
Жаркое со свининой и картофелем|Печеня зі свининою та картоплею|Pork and potato stew|pork:500 potato:900 carrot:150 onion:150 oil:35 broth:200|жаркое,печеня
Жаркое с говядиной|Печеня з яловичиною|Beef and potato stew|beef:500 potato:900 carrot:150 onion:150 oil:30 broth:250
Курица тушеная с овощами|Курка тушкована з овочами|Chicken and vegetable stew|chicken:700 carrot:200 onion:200 tomato:300 pepper:300 oil:25
Печень с луком|Печінка з цибулею|Liver with onions|liver:700 onion:300 oil:40
Печеночные оладьи|Печінкові оладки|Liver pancakes|liver:600 onion:150 flour:100 egg:100 sourcream:100 oil:35
Печень в сметанном соусе|Печінка у сметанному соусі|Liver in sour cream sauce|liver:650 onion:200 sourcream:200 flour:30 oil:20
Свинина тушеная с капустой|Свинина тушкована з капустою|Pork and cabbage stew|pork:500 cabbage:1000 carrot:150 onion:150 oil:25
Утка с яблоками|Качка з яблуками|Duck with apples|duck:800 apple:500 onion:100
Домашняя колбаса с гарниром|Домашня ковбаса з гарніром|Sausage with potatoes|sausage:600 potato:500 onion:150 oil:20
Плов с курицей|Плов із куркою|Chicken pilaf|rice:1000 chicken:450 carrot:300 onion:250 oil:60
Плов со свининой|Плов зі свининою|Pork pilaf|rice:1000 pork:450 carrot:300 onion:250 oil:60
Гречка с мясом по-домашнему|Гречка з м’ясом по-домашньому|Buckwheat with meat|buckwheat:1000 pork:400 carrot:150 onion:150 oil:30
''')
group('Мясные блюда','Закарпатская','''
Гуляш закарпатский|Гуляш закарпатський|Transcarpathian goulash|beef:600 pork:200 onion:250 tomato:200 pepper:250 lard:30 broth:200
Паприкаш куриный|Паприкаш курячий|Chicken paprikash|chicken:800 onion:250 pepper:200 sourcream:250 flour:30 broth:200|паприкаш,paprikash
Паприкаш свиной|Паприкаш свинячий|Pork paprikash|pork:800 onion:250 pepper:200 sourcream:200 flour:30 broth:200
Сегединский гуляш|Сегединський гуляш|Szegedin goulash|pork:700 sauerkraut:700 onion:200 sourcream:200 oil:20
Токань мясной|Токань м’ясний|Transcarpathian meat tokany|pork:700 onion:250 pepper:200 tomato:200 oil:25 broth:150|токань,tokany
Голубцы закарпатские|Голубці закарпатські|Transcarpathian cabbage rolls|sauerkraut:800 mince:450 rice:450 ham:150 onion:150 paste:70 broth:200
''')
group('Мясные блюда','Кавказская','''
Шашлык из свинины|Шашлик зі свинини|Pork shashlik|pork:850 onion:150 lemon:30|шашлык,шашлик,kebab,shashlik
Шашлык из курицы|Шашлик із курки|Chicken shashlik|chicken:850 onion:150 yogurt:100
Шашлык из индейки|Шашлик з індички|Turkey shashlik|turkey:850 onion:150 yogurt:80
Шашлык из говядины|Шашлик із яловичини|Beef shashlik|beef:850 onion:150 lemon:30
Шашлык из баранины|Шашлик із баранини|Lamb shashlik|lamb:850 onion:150 lemon:30
Люля-кебаб|Люля-кебаб|Lula kebab|lamb:600 beef:200 onion:150 lard:50|люля,люля кебаб
''',200)
group('Мясные блюда','Грузинская','''
Чахохбили|Чахохбілі|Chakhokhbili chicken stew|chicken:800 tomato:600 onion:300 butter:30 garlic:15
Чкмерули|Чкмерулі|Chkmeruli chicken in garlic cream|chicken:900 cream:300 milk:200 garlic:30 butter:30
Оджахури|Оджахурі|Ojakhuri pork and potatoes|pork:600 potato:800 onion:250 oil:45 garlic:15
''')
group('Мясные блюда','Европейская','''
Бефстроганов|Бефстроганов|Beef stroganoff|beef:700 onion:250 sourcream:200 mushroom:250 flour:30 butter:25
Гуляш говяжий с подливой|Гуляш яловичий з підливою|Beef goulash with gravy|beef:700 onion:250 carrot:150 paste:80 flour:30 broth:300 oil:25
Шницель венский|Шніцель віденський|Viennese schnitzel|beef:700 breadcrumbs:150 egg:150 oil:50
Стейк говяжий|Стейк яловичий|Beef steak|beef:950 butter:30
Стейк из индейки|Стейк з індички|Turkey steak|turkey:950 oil:25
Мясо по-французски|М’ясо по-французьки|Pork baked with cheese|pork:650 onion:250 cheese:200 mayo:100
Ребрышки в медовом соусе|Реберця в медовому соусі|Ribs in honey glaze|pork:850 honey:80 soy:60 garlic:15
Куриные крылышки запеченные|Курячі крильця запечені|Baked chicken wings|chicken:700 oil:45 honey:60 soy:50
''')
group('Рыбные блюда','Украинская','''
Рыбные котлеты|Рибні котлети|Fish patties|whitefish:700 bread:150 egg:100 onion:150 oil:35
Рыбные котлеты на пару|Рибні котлети на парі|Steamed fish patties|whitefish:700 bread:120 egg:100 onion:150
Рыба жареная в панировке|Риба смажена в паніруванні|Breaded fried fish|whitefish:750 flour:100 egg:100 oil:45
Рыба тушеная с овощами|Риба тушкована з овочами|Fish stewed with vegetables|whitefish:700 carrot:250 onion:250 tomato:300 oil:25
Карп запеченный с овощами (без костей)|Короп запечений з овочами (без кісток)|Boneless baked carp with vegetables|carp:800 onion:200 carrot:200 sourcream:150 oil:20
Скумбрия запеченная (без костей)|Скумбрія запечена (без кісток)|Boneless baked mackerel|mackerel:950 onion:150 lemon:40
Судак в сметане|Судак у сметані|Zander in sour cream|pikeperch:800 sourcream:200 onion:150 oil:20
Сельдь с картофелем|Оселедець із картоплею|Herring with potatoes|herring:350 potato:700 onion:150 oil:20
''')
group('Рыбные блюда','Средиземноморская','''
Лосось запеченный с лимоном|Лосось запечений з лимоном|Baked salmon with lemon|salmon:950 lemon:50 oil:15
Рыба на гриле с овощами|Риба на грилі з овочами|Grilled fish with vegetables|whitefish:700 pepper:200 zucchini:200 tomato:200 oil:30
Креветки с чесноком|Креветки з часником|Garlic shrimp|shrimp:800 garlic:30 butter:60 lemon:50
Кальмары в сметанном соусе|Кальмари у сметанному соусі|Squid in sour cream sauce|squid:700 sourcream:250 onion:200 oil:20
Мидии в сливочном соусе|Мідії у вершковому соусі|Mussels in cream sauce|mussels:700 cream:250 onion:150 garlic:20
''')
group('Гарниры','Украинская','''
Картофельное пюре с молоком|Картопляне пюре з молоком|Mashed potatoes with milk|potato:850 milk:150 butter:40|пюре,толченка,товчена картопля
Картофельное пюре на воде|Картопляне пюре на воді|Dairy-free mashed potatoes|potato:900 water:100 oil:15
Картофель жареный с луком|Картопля смажена з цибулею|Fried potatoes with onions|potato:850 onion:150 oil:75
Картофель по-селянски|Картопля по-селянськи|Rustic baked potato wedges|potato:950 oil:40 garlic:15|по деревенски,по-селянськи
Картофель запеченный с сыром|Картопля запечена із сиром|Cheesy baked potatoes|potato:850 cheese:150 sourcream:150
Картофель тушеный с грибами|Картопля тушкована з грибами|Potatoes stewed with mushrooms|potato:800 mushroom:350 onion:150 oil:30
Гречневая каша с маслом|Гречана каша з маслом|Buckwheat with butter|buckwheat:1000 butter:35
Гречка с грибами|Гречка з грибами|Buckwheat with mushrooms|buckwheat:850 mushroom:350 onion:150 oil:30
Рис с овощами|Рис з овочами|Rice with vegetables|rice:800 carrot:150 pepper:150 corn:150 oil:25
Рис с маслом|Рис з маслом|Rice with butter|rice:1000 butter:30
Пшенная каша с тыквой|Пшоняна каша з гарбузом|Millet porridge with pumpkin|millet:700 pumpkin:400 milk:250 butter:25
Перловая каша с грибами|Перлова каша з грибами|Barley with mushrooms|barley:800 mushroom:350 onion:150 oil:25
Гороховое пюре|Горохове пюре|Split pea puree|peas:950 butter:25 water:100
Фасоль тушеная в томате|Квасоля тушкована в томаті|Beans in tomato sauce|beans:850 tomato:250 onion:150 paste:70 oil:25
Капуста тушеная|Капуста тушкована|Stewed cabbage|cabbage:1000 carrot:200 onion:150 paste:80 oil:40
Овощное рагу|Овочеве рагу|Vegetable stew|potato:350 zucchini:350 carrot:150 onion:150 pepper:200 tomato:250 oil:40
Баклажаны тушеные с овощами|Баклажани тушковані з овочами|Eggplant vegetable stew|eggplant:700 pepper:250 tomato:300 onion:150 oil:50
Кабачки тушеные в сметане|Кабачки тушковані у сметані|Zucchini in sour cream|zucchini:900 sourcream:200 onion:150 oil:20
Цветная капуста в сухарях|Цвітна капуста в сухарях|Breaded cauliflower|cauliflower:850 breadcrumbs:120 egg:100 oil:35
Брокколи с сыром|Броколі із сиром|Broccoli with cheese|broccoli:900 cheese:150 butter:20
''',200)
group('Гарниры','Закарпатская','''
Банош с брынзой|Банош із бринзою|Banosh with brined cheese|cornmeal:700 sourcream:250 feta:150 butter:30|банош,бануш,banosh,banush
Банош со шкварками|Банош зі шкварками|Banosh with pork cracklings|cornmeal:700 sourcream:250 bacon:130 feta:100
Токан с брынзой|Токан із бринзою|Cornmeal tokan with cheese|cornmeal:900 feta:180 butter:25|токан,токан кукурудзяний
Кромпли с луком|Крумплі з цибулею|Transcarpathian potatoes with onions|potato:900 onion:200 butter:40
Тушеная квашеная капуста по-закарпатски|Тушкована квашена капуста по-закарпатськи|Transcarpathian stewed sauerkraut|sauerkraut:1000 onion:200 bacon:100 oil:20
''',200)
group('Гарниры','Международная','''
Булгур с овощами|Булгур з овочами|Bulgur with vegetables|bulgur:850 pepper:200 tomato:200 onion:100 oil:25
Кускус с овощами|Кускус з овочами|Couscous with vegetables|couscous:850 zucchini:200 pepper:200 onion:100 oil:25
Киноа с овощами|Кіноа з овочами|Quinoa with vegetables|quinoa:850 pepper:200 broccoli:200 oil:25
Чечевица с овощами|Сочевиця з овочами|Lentils with vegetables|lentils:850 carrot:150 onion:150 tomato:250 oil:25
Пюре из цветной капусты|Пюре із цвітної капусти|Cauliflower mash|cauliflower:1000 butter:35 cream:100
Пюре из тыквы|Пюре з гарбуза|Pumpkin puree|pumpkin:1000 butter:25 milk:100
Овощи на гриле|Овочі на грилі|Grilled vegetables|zucchini:350 eggplant:350 pepper:300 tomato:200 oil:35
Рататуй|Рататуй|Ratatouille|zucchini:400 eggplant:400 tomato:400 pepper:300 onion:150 oil:50
''',200)
group('Вареники и тесто','Украинская','''
Вареники с картофелем|Вареники з картоплею|Potato varenyky|flour:350 water:180 egg:50 potato:500 onion:150 oil:20|вареники,вареники з картоплею,dumplings,varenyky
Вареники с картофелем и грибами|Вареники з картоплею та грибами|Potato and mushroom varenyky|flour:350 water:180 egg:50 potato:400 mushroom:200 onion:150 oil:20
Вареники с творогом соленые|Вареники із солоним сиром|Savory cottage cheese varenyky|flour:350 water:180 egg:100 curd:600
Вареники с творогом сладкие|Вареники із солодким сиром|Sweet cottage cheese varenyky|flour:350 water:180 egg:100 curd:600 sugar:80
Вареники с вишней|Вареники з вишнею|Cherry varenyky|flour:350 water:180 egg:50 cherry:600 sugar:100
Вареники с капустой|Вареники з капустою|Cabbage varenyky|flour:350 water:180 egg:50 cabbage:600 onion:150 oil:30
Вареники с фасолью|Вареники з квасолею|Bean varenyky|flour:350 water:180 egg:50 beans:600 onion:150 oil:20
Вареники с мясом|Вареники з м’ясом|Meat varenyky|flour:350 water:180 egg:50 mince:500 onion:150
Ленивые вареники|Ліниві вареники|Lazy cottage cheese dumplings|curd:600 flour:180 egg:100 sugar:50 butter:30
Галушки полтавские|Галушки полтавські|Poltava halushky|flour:400 egg:100 kefir:250 butter:40|галушки,halushky
Галушки с курицей|Галушки з куркою|Halushky with chicken|flour:300 egg:100 kefir:200 chicken:400 sourcream:150
Пельмени домашние|Пельмені домашні|Homemade meat dumplings|flour:350 water:180 egg:50 mince:500 onion:150
Налистники с творогом|Налисники із сиром|Cottage cheese nalysnyky|flour:200 milk:400 egg:150 curd:500 sugar:70 butter:40|налистники,налисники,nalysnyky
Налистники с мясом|Налисники з м’ясом|Meat nalysnyky|flour:200 milk:400 egg:150 mince:500 onion:150 oil:35
Налистники с грибами|Налисники з грибами|Mushroom nalysnyky|flour:200 milk:400 egg:150 mushroom:500 onion:150 sourcream:150 oil:25
Деруны со сметаной|Деруни зі сметаною|Potato deruny with sour cream|potato:700 flour:100 egg:100 onion:150 oil:50 sourcream:150|драники,деруны,деруни,deruny
Деруны с грибами|Деруни з грибами|Mushroom potato deruny|potato:650 flour:100 egg:100 mushroom:250 onion:150 oil:45
Деруны с мясом|Деруни з м’ясом|Meat-filled potato pancakes|potato:650 flour:100 egg:100 mince:300 onion:150 oil:45
Картопляники с мясом|Картопляники з м’ясом|Potato cakes with meat|potato:700 flour:100 egg:100 mince:300 onion:150 oil:40|картопляники,зразы,зрази
Картопляники с грибами|Картопляники з грибами|Potato cakes with mushrooms|potato:700 flour:100 egg:100 mushroom:350 onion:150 oil:35
''',250)
group('Вареники и тесто','Закарпатская','''
Кнедлики хлебные|Кнедлики хлібні|Bread knedliky|flour:400 milk:250 egg:100 bread:200 butter:25|кнедлики,кнедлі,knedliky
Кнедлики с мясной подливой|Кнедлики з м’ясною підливою|Knedliky with meat gravy|flour:250 milk:200 egg:100 bread:150 beef:350 onion:150 sourcream:150 broth:200
Кнедлики с грибным соусом|Кнедлики з грибним соусом|Knedliky with mushroom sauce|flour:250 milk:200 egg:100 bread:150 mushroom:400 sourcream:200 onion:150
Гомбовцы творожные|Гомбовці сирні|Cottage cheese hombovtsi|curd:600 semolina:120 egg:100 sugar:70 breadcrumbs:70 butter:40|гомбовцы,гомбовці,hombovtsi
Гомбовцы со сливой|Гомбовці зі сливою|Plum hombovtsi|potato:500 flour:250 egg:100 plum:450 sugar:100 breadcrumbs:100 butter:50
Гомбовцы с абрикосом|Гомбовці з абрикосом|Apricot hombovtsi|potato:500 flour:250 egg:100 apricot:450 sugar:100 breadcrumbs:100 butter:50
Страпачки с брынзой|Страпачки із бринзою|Bryndza strapachky|potato:500 flour:300 egg:100 feta:250 bacon:80|страпачки,strapachky
Шпецле с сыром|Шпецле із сиром|Cheese spaetzle|flour:400 egg:200 milk:150 cheese:200 butter:40
''',250)
group('Завтраки','Украинская','''
Сырники жареные|Сирники смажені|Fried syrnyky|curd:600 flour:120 egg:100 sugar:70 oil:35|сырники,сирники,syrnyky
Сырники запеченные|Сирники запечені|Baked syrnyky|curd:600 flour:100 egg:100 sugar:60 butter:15
Сырники с изюмом|Сирники з родзинками|Syrnyky with raisins|curd:600 flour:120 egg:100 sugar:40 raisins:100 oil:30
Оладьи на кефире|Оладки на кефірі|Kefir pancakes|flour:300 kefir:400 egg:100 sugar:60 oil:40
Оладьи яблочные|Оладки яблучні|Apple pancakes|flour:250 kefir:300 egg:100 apple:350 sugar:40 oil:30
Оладьи кабачковые|Оладки кабачкові|Zucchini pancakes|zucchini:700 flour:150 egg:150 oil:40
Омлет с молоком|Омлет із молоком|Milk omelette|egg:600 milk:250 butter:25
Омлет с овощами|Омлет з овочами|Vegetable omelette|egg:500 milk:150 tomato:200 pepper:150 oil:20
Омлет с сыром|Омлет із сиром|Cheese omelette|egg:500 milk:150 cheese:150 butter:20
Яичница с помидорами|Яєчня з помідорами|Eggs with tomatoes|egg:500 tomato:400 onion:100 oil:25
Яичница с беконом|Яєчня з беконом|Eggs with bacon|egg:500 bacon:150 butter:15
Овсянка на молоке|Вівсянка на молоці|Oatmeal with milk|oats:700 milk:300 butter:20
Овсянка с бананом|Вівсянка з бананом|Oatmeal with banana|oats:800 banana:250 honey:25
Манная каша на молоке|Манна каша на молоці|Semolina porridge with milk|semolina:100 milk:800 sugar:40 butter:25 water:200
Рисовая молочная каша|Рисова молочна каша|Milk rice porridge|rice:650 milk:400 sugar:40 butter:25
Кукурузная молочная каша|Кукурудзяна молочна каша|Milk cornmeal porridge|cornmeal:700 milk:350 sugar:40 butter:25
Гренки сладкие|Грінки солодкі|Sweet French toast|bread:500 egg:200 milk:250 sugar:50 butter:40
''',200)
group('Завтраки','Международная','''
Шакшука|Шакшука|Shakshuka|egg:450 tomato:700 pepper:250 onion:150 oil:35
Панкейки|Панкейки|Pancakes|flour:300 milk:400 egg:150 sugar:60 butter:40
Тост с авокадо и яйцом|Тост з авокадо та яйцем|Avocado and egg toast|bread:300 avocado:250 egg:250 lemon:20
Тост с лососем и сыром|Тост із лососем та сиром|Salmon and cheese toast|bread:300 salmon:250 curd:200 cucumber:150
Йогурт с ягодами и овсянкой|Йогурт з ягодами та вівсянкою|Yogurt berry oat bowl|yogurt:500 berries:250 oats:250 honey:25
''',200)
group('Салаты','Украинская','''
Винегрет|Вінегрет|Vinaigrette beet salad|beet:350 potato:300 carrot:150 pickle:200 beans:150 onion:80 oil:45
Оливье с курицей|Олів’є з куркою|Chicken Olivier salad|chicken:250 potato:300 carrot:150 egg:200 pickle:200 mayo:150
Оливье с колбасой|Олів’є з ковбасою|Sausage Olivier salad|sausage:250 potato:300 carrot:150 egg:200 pickle:200 mayo:150
Сельдь под шубой|Оселедець під шубою|Herring under a fur coat|herring:250 beet:400 potato:300 carrot:200 egg:150 mayo:180 onion:80|шуба,селедка под шубой
Салат из капусты с морковью|Салат із капусти та моркви|Cabbage carrot salad|cabbage:700 carrot:250 oil:30 sugar:15
Салат из огурцов и помидоров|Салат з огірків та помідорів|Cucumber tomato salad|cucumber:500 tomato:500 onion:100 oil:30
Салат овощной со сметаной|Салат овочевий зі сметаною|Vegetable salad with sour cream|cucumber:500 tomato:500 onion:100 sourcream:150
Салат свекольный с чесноком|Салат буряковий із часником|Beetroot garlic salad|beet:800 garlic:25 walnuts:80 mayo:100
Салат свекольный с черносливом|Салат буряковий із чорносливом|Beetroot prune salad|beet:700 prunes:150 walnuts:80 sourcream:150
Морковь по-корейски|Морква по-корейськи|Korean-style carrot salad|carrot:1000 garlic:30 oil:60 sugar:20
Салат крабовый|Салат крабовий|Surimi crab salad|crab:300 corn:300 egg:250 rice:200 mayo:150 cucumber:200
Салат с фасолью и сухариками|Салат із квасолею та сухариками|Bean and crouton salad|beans:600 bread:150 tomato:250 mayo:100 garlic:20
Салат с курицей и грибами|Салат із куркою та грибами|Chicken mushroom salad|chicken:400 mushroom:350 egg:200 onion:100 mayo:120
Салат с печенью|Салат із печінкою|Liver salad|liver:400 carrot:200 pickle:200 onion:150 mayo:100
''',200)
group('Салаты','Средиземноморская','''
Греческий салат|Грецький салат|Greek salad|tomato:400 cucumber:400 pepper:200 feta:200 olives:80 oil:30
Цезарь с курицей|Цезар із куркою|Chicken Caesar salad|chicken:350 lettuce:450 bread:150 cheese:70 mayo:100 mustard:20 lemon:30
Цезарь с креветками|Цезар із креветками|Shrimp Caesar salad|shrimp:350 lettuce:450 bread:150 cheese:70 mayo:100 mustard:20 lemon:30
Капрезе|Капрезе|Caprese|tomato:600 mozzarella:350 oil:25
Салат с тунцом|Салат із тунцем|Tuna salad|tuna:300 lettuce:250 tomato:250 cucumber:250 egg:150 oil:25
Салат с авокадо|Салат з авокадо|Avocado salad|avocado:300 tomato:400 cucumber:300 lettuce:200 oil:20 lemon:30
Табуле|Табуле|Tabbouleh|bulgur:500 tomato:400 cucumber:300 onion:100 lemon:60 oil:40
''',200)
group('Салаты','Международная','''
Коул-слоу|Коул-слоу|Coleslaw|cabbage:700 carrot:250 mayo:100 yogurt:100 sugar:20
Салат с киноа|Салат із кіноа|Quinoa salad|quinoa:600 tomato:300 cucumber:300 feta:120 oil:25
Салат с нутом|Салат із нутом|Chickpea salad|chickpeas:600 tomato:300 cucumber:300 onion:100 oil:25 lemon:30
''',200)
group('Выпечка','Украинская','''
Пампушки с чесноком|Пампушки з часником|Garlic pampushky|flour:500 water:280 sugar:30 oil:45 garlic:30|пампушки,pampushky
Пирожки с картофелем печеные|Пиріжки з картоплею печені|Baked potato buns|flour:400 milk:250 egg:100 butter:50 potato:550 onion:150 oil:20
Пирожки с капустой печеные|Пиріжки з капустою печені|Baked cabbage buns|flour:400 milk:250 egg:100 butter:50 cabbage:650 onion:150 oil:25
Пирожки с мясом печеные|Пиріжки з м’ясом печені|Baked meat buns|flour:400 milk:250 egg:100 butter:50 mince:500 onion:150
Пирожки с картофелем жареные|Пиріжки з картоплею смажені|Fried potato buns|flour:400 kefir:250 egg:100 potato:550 onion:150 oil:90
Пирожки с вишней|Пиріжки з вишнею|Cherry buns|flour:400 milk:250 egg:100 butter:50 cherry:550 sugar:150
Пирожки с творогом|Пиріжки із сиром|Cottage cheese buns|flour:400 milk:250 egg:100 butter:50 curd:550 sugar:100
Пирог с яблоками|Пиріг із яблуками|Apple pie|flour:350 egg:200 butter:150 sugar:180 apple:700
Шарлотка яблочная|Шарлотка яблучна|Apple sponge cake|flour:250 egg:300 sugar:200 apple:700
Пирог с капустой|Пиріг із капустою|Cabbage pie|flour:350 kefir:400 egg:200 cabbage:700 onion:150 oil:40
Пирог с курицей и грибами|Пиріг із куркою та грибами|Chicken mushroom pie|flour:350 butter:120 egg:200 chicken:400 mushroom:350 sourcream:200
Вергуны|Вергуни|Verhuny fried pastries|flour:400 kefir:250 egg:100 sugar:100 oil:100|вергуны,вергуни,verhuny
Медовые пряники|Медові пряники|Honey gingerbread|flour:500 honey:250 sugar:120 egg:150 butter:80
''',100)
group('Выпечка','Закарпатская','''
Ретеш с яблоками|Ретеш із яблуками|Transcarpathian apple retes|flour:350 water:180 oil:50 apple:800 sugar:150 breadcrumbs:70|ретеш,retes,retesh,strudel
Ретеш с творогом|Ретеш із сиром|Transcarpathian cottage cheese retes|flour:350 water:180 oil:50 curd:700 sugar:130 egg:100
Ретеш с маком|Ретеш із маком|Poppy seed retes|flour:350 water:180 oil:50 poppy:250 milk:200 sugar:180
Кифлики с орехами|Кіфлики з горіхами|Walnut kiflyky|flour:400 butter:200 sourcream:150 walnuts:250 sugar:150|кифлики,кіфлики,kiflyky
Кифлики с повидлом|Кіфлики з повидлом|Jam kiflyky|flour:400 butter:200 sourcream:150 jam:350 sugar:70
Погачи с сыром|Погачі із сиром|Cheese pogachi|flour:450 butter:150 sourcream:200 cheese:250 egg:100|погачи,погачі,pogacsa
''',100)
group('Десерты','Украинская','''
Творожная запеканка|Сирна запіканка|Cottage cheese casserole|curd:800 egg:200 semolina:100 sugar:100 sourcream:150 butter:20|творожная,сирна,запеканка,запіканка
Творожная запеканка с изюмом|Сирна запіканка з родзинками|Cottage cheese raisin casserole|curd:800 egg:200 semolina:100 sugar:70 raisins:150 sourcream:150
Творожная запеканка с яблоками|Сирна запіканка з яблуками|Cottage cheese apple casserole|curd:700 egg:200 semolina:100 apple:400 sugar:70 sourcream:100
Творожный десерт с ягодами|Сирний десерт із ягодами|Cottage cheese berry dessert|curd:650 yogurt:200 berries:300 honey:50
Творожный десерт с бананом|Сирний десерт із бананом|Cottage cheese banana dessert|curd:650 yogurt:200 banana:300 honey:30
Творожный десерт с какао|Сирний десерт із какао|Cocoa cottage cheese dessert|curd:700 yogurt:250 cocoa:40 honey:60
Творожная масса с изюмом|Сиркова маса з родзинками|Sweet curd with raisins|curd:800 sourcream:100 raisins:150 sugar:80
Сочники с творогом|Сочники із сиром|Cottage cheese sochniki|flour:400 butter:150 egg:200 sugar:150 curd:600 sourcream:150
Кутья с орехами и медом|Кутя з горіхами та медом|Kutia with nuts and honey|barley:700 poppy:150 walnuts:150 raisins:150 honey:150 water:150|кутья,кутя,kutia
Яблоки запеченные с творогом|Яблука запечені із сиром|Apples baked with cottage cheese|apple:800 curd:300 honey:50 walnuts:50
Яблоки запеченные с медом|Яблука запечені з медом|Honey baked apples|apple:900 honey:80 walnuts:60
Медовик|Медовик|Honey layer cake|flour:450 egg:200 butter:150 sugar:250 honey:150 sourcream:800
Наполеон|Наполеон|Napoleon cake|flour:500 butter:300 milk:800 egg:200 sugar:250
Маковый рулет|Маковий рулет|Poppy seed roll|flour:450 milk:250 butter:100 egg:150 poppy:250 sugar:200
Львовский сырник|Львівський сирник|Lviv syrnyk cheesecake|curd:900 egg:250 sugar:200 butter:150 raisins:100 chocolate:100 sourcream:100
Молочный кисель|Молочний кисіль|Milk pudding|milk:900 starch:60 sugar:80
Кисель ягодный|Кисіль ягідний|Berry kissel|berries:400 water:1000 starch:80 sugar:150
''',150)
group('Десерты','Закарпатская','''
Палачинты с творогом|Палачинти із сиром|Cottage cheese palachynty|flour:250 milk:500 egg:200 curd:600 sugar:100 butter:50|палачинты,палачинти,palachynty
Палачинты с орехами|Палачинти з горіхами|Walnut palachynty|flour:250 milk:500 egg:200 walnuts:250 sugar:150 butter:50
Гомбовцы с ягодами|Гомбовці з ягодами|Berry hombovtsi|curd:650 semolina:120 egg:100 berries:300 sugar:100 breadcrumbs:70 butter:30
''',150)
group('Десерты','Европейская','''
Чизкейк творожный|Чізкейк сирний|Cottage cheese cheesecake|curd:800 cream:250 egg:200 sugar:180 breadcrumbs:200 butter:100
Тирамису (упрощенный)|Тірамісу (спрощений)|Simplified tiramisu|curd:600 cream:300 egg:200 sugar:180 bread:250 cocoa:30
Панна-котта (без добавок)|Пана-кота (без добавок)|Plain panna cotta|cream:600 milk:300 sugar:120
Рисовый пудинг|Рисовий пудинг|Rice pudding|rice:700 milk:500 egg:150 sugar:120 butter:25
Брауни|Брауні|Brownies|chocolate:300 butter:180 sugar:220 egg:250 flour:180 cocoa:50
Крамбл яблочный|Крамбл яблучний|Apple crumble|apple:900 flour:250 butter:160 sugar:180 oats:200
Мороженое домашнее сливочное|Морозиво домашнє вершкове|Homemade cream ice cream|cream:600 milk:300 sugar:180 egg:100
Йогуртовый десерт с фруктами|Йогуртовий десерт із фруктами|Fruit yogurt dessert|yogurt:650 apple:200 banana:200 honey:40
''',150)
group('Закуски','Украинская','''
Паштет печеночный|Паштет печінковий|Liver pate|liver:700 onion:200 carrot:200 butter:100
Форшмак|Форшмак|Forshmak herring spread|herring:450 apple:200 egg:200 bread:100 butter:60 onion:100
Кабачковая икра|Кабачкова ікра|Zucchini spread|zucchini:1000 carrot:250 onion:250 paste:120 oil:70
Икра баклажанная|Ікра баклажанна|Eggplant spread|eggplant:900 tomato:400 pepper:250 onion:200 oil:60
Грибы в сметане|Гриби у сметані|Mushrooms in sour cream|mushroom:900 sourcream:250 onion:200 oil:25
Печеночный торт|Печінковий торт|Savory liver pancake cake|liver:700 egg:200 flour:150 milk:200 carrot:300 onion:250 mayo:200 oil:45
Бутерброд с ветчиной и сыром|Бутерброд із шинкою та сиром|Ham and cheese sandwich|bread:400 ham:300 cheese:150 butter:30 cucumber:150
Гренки чесночные к пиву|Грінки часникові до пива|Garlic beer-snack croutons|ryebread:600 oil:80 garlic:40|пивные,пивні,к пиву,до пива
Сырные палочки в панировке|Сирні палички в паніруванні|Breaded cheese sticks|cheese:600 breadcrumbs:180 egg:150 oil:60|пивные закуски,пивні закуски
Луковые кольца|Цибулеві кільця|Onion rings|onion:800 flour:200 egg:100 milk:150 oil:80
Куриные наггетсы|Курячі нагетси|Chicken nuggets|chicken:700 breadcrumbs:200 egg:150 oil:60
''',100)
group('Закуски','Средиземноморская','''
Хумус|Хумус|Hummus|chickpeas:800 sesame:100 oil:60 lemon:80 garlic:25
Брускетта с томатами|Брускета з томатами|Tomato bruschetta|bread:400 tomato:500 oil:40 garlic:20
Рулетики из баклажанов с орехами|Рулетики з баклажанів з горіхами|Eggplant walnut rolls|eggplant:800 walnuts:200 garlic:25 oil:60
''',100)
group('Закуски','Международная','''
Фалафель|Фалафель|Falafel|chickpeas:850 onion:150 flour:100 garlic:25 oil:70
Шаурма с курицей|Шаурма з куркою|Chicken shawarma|lavash:350 chicken:450 cabbage:250 cucumber:200 tomato:200 yogurt:180 mayo:60
Шаурма со свининой|Шаурма зі свининою|Pork shawarma|lavash:350 pork:450 cabbage:250 cucumber:200 tomato:200 yogurt:180 mayo:60
Кесадилья с курицей|Кесадилья з куркою|Chicken quesadilla|tortilla:400 chicken:450 cheese:250 pepper:200
Буррито с фасолью|Бурито з квасолею|Bean burrito|tortilla:400 beans:500 rice:300 tomato:200 cheese:150
Гамбургер домашний|Гамбургер домашній|Homemade hamburger|bread:400 beef:450 lettuce:100 tomato:200 pickle:100 mayo:60
Чизбургер домашний|Чізбургер домашній|Homemade cheeseburger|bread:400 beef:450 cheese:150 lettuce:100 tomato:200 pickle:100 mayo:60
''',250)
group('Паста и пицца','Итальянская','''
Спагетти болоньезе|Спагеті болоньєзе|Spaghetti bolognese|pasta:1000 mince:450 tomato:500 onion:150 carrot:100 oil:25 cheese:80|болоньезе,bolognese
Паста карбонара|Паста карбонара|Pasta carbonara|pasta:1000 bacon:250 egg:250 cheese:150|карбонара,carbonara
Паста с курицей и грибами|Паста з куркою та грибами|Chicken mushroom pasta|pasta:1000 chicken:400 mushroom:350 cream:250 cheese:100
Паста с тунцом|Паста з тунцем|Tuna pasta|pasta:1000 tuna:350 tomato:400 oil:30
Паста с креветками|Паста з креветками|Shrimp pasta|pasta:1000 shrimp:400 cream:250 cheese:100 garlic:20
Паста с овощами|Паста з овочами|Vegetable pasta|pasta:1000 zucchini:300 tomato:300 pepper:250 oil:40
Макароны с сыром|Макарони із сиром|Macaroni and cheese|pasta:1000 cheese:250 milk:250 butter:40
Лазанья мясная|Лазанья м’ясна|Meat lasagna|pasta:700 mince:500 tomato:500 milk:400 flour:50 butter:50 cheese:250
Ризотто с грибами|Різото з грибами|Mushroom risotto|rice:1000 mushroom:500 onion:200 butter:60 cheese:100 broth:200
Ризотто с курицей|Різото з куркою|Chicken risotto|rice:1000 chicken:450 onion:200 butter:50 cheese:100 broth:200
Пицца Маргарита|Піца Маргарита|Margherita pizza|flour:400 water:230 oil:30 tomato:300 mozzarella:300|пицца,піца,pizza
Пицца с ветчиной и грибами|Піца з шинкою та грибами|Ham mushroom pizza|flour:400 water:230 oil:30 tomato:250 mozzarella:250 ham:200 mushroom:200
Пицца четыре сыра|Піца чотири сири|Four-cheese pizza|flour:400 water:230 oil:30 mozzarella:250 cheese:200 feta:100
Пицца с курицей|Піца з куркою|Chicken pizza|flour:400 water:230 oil:30 tomato:250 mozzarella:250 chicken:250
''',250)
group('Международные блюда','Грузинская','''
Хачапури по-аджарски|Хачапурі по-аджарськи|Adjarian khachapuri|flour:450 milk:250 egg:200 feta:400 mozzarella:200 butter:60|хачапури,хачапурі,khachapuri
Хачапури по-имеретински|Хачапурі по-імеретинськи|Imeretian khachapuri|flour:450 yogurt:300 feta:500 egg:100 butter:50
Хинкали с мясом|Хінкалі з м’ясом|Meat khinkali|flour:400 water:230 beef:400 pork:200 onion:250 broth:200|хинкали,хінкалі,khinkali
Лобио|Лобіо|Lobio|beans:1000 onion:250 walnuts:100 oil:35 garlic:25
Сациви|Сациві|Satsivi|chicken:700 walnuts:250 onion:200 broth:400 garlic:25
''',250)
group('Международные блюда','Среднеазиатская','''
Плов с бараниной|Плов із бараниною|Lamb pilaf|rice:1000 lamb:500 carrot:400 onion:300 oil:80
Манты с мясом|Манти з м’ясом|Meat manti|flour:400 water:230 egg:50 mince:650 onion:350
Лагман с говядиной|Лагман з яловичиною|Beef lagman|noodles:800 beef:450 pepper:250 tomato:300 onion:200 carrot:150 broth:300 oil:35
Самса с мясом|Самса з м’ясом|Meat samsa|flour:450 water:220 butter:150 mince:650 onion:350
''',250)
group('Международные блюда','Азиатская','''
Рис жареный с яйцом|Рис смажений із яйцем|Egg fried rice|rice:1000 egg:250 carrot:150 corn:150 soy:60 oil:45
Рис жареный с курицей|Рис смажений з куркою|Chicken fried rice|rice:1000 chicken:350 egg:200 carrot:150 soy:60 oil:45
Лапша вок с курицей|Локшина вок із куркою|Chicken wok noodles|noodles:1000 chicken:350 pepper:250 carrot:150 soy:70 oil:40
Лапша вок с овощами|Локшина вок з овочами|Vegetable wok noodles|noodles:1000 pepper:250 carrot:200 cabbage:250 soy:70 oil:40
Курица терияки с рисом|Курка теріякі з рисом|Teriyaki chicken with rice|chicken:600 rice:900 soy:100 honey:80 oil:30
Курица карри с рисом|Курка карі з рисом|Chicken curry with rice|chicken:600 rice:900 coconut:350 onion:200 tomato:200 oil:25
Суши с лососем (упрощенные)|Суші з лососем (спрощені)|Simple salmon sushi|rice:650 salmon:350 sugar:35
Роллы с лососем и огурцом (упрощенные)|Роли з лососем та огірком (спрощені)|Simple salmon cucumber rolls|rice:650 salmon:250 cucumber:200 sugar:30
Ролл Филадельфия (адаптированный)|Рол Філадельфія (адаптований)|Adapted Philadelphia roll|rice:600 salmon:250 curd:180 cucumber:150 sugar:30
Тофу с овощами и рисом|Тофу з овочами та рисом|Tofu vegetables and rice|tofu:500 rice:800 pepper:250 broccoli:250 soy:60 oil:30
''',250)
group('Международные блюда','Европейская','''
Бигос|Бігос|Bigos|sauerkraut:650 cabbage:650 pork:450 sausage:250 prunes:100 onion:200 paste:80
Запеканка картофельная с мясом|Запіканка картопляна з м’ясом|Potato meat casserole|potato:1000 mince:500 onion:200 egg:200 milk:250 cheese:150 butter:30
Запеканка овощная|Запіканка овочева|Vegetable casserole|zucchini:500 broccoli:500 cauliflower:400 egg:300 milk:250 cheese:150
Мусака (адаптированная)|Мусака (адаптована)|Adapted moussaka|eggplant:700 potato:500 mince:500 tomato:350 milk:350 cheese:200 flour:40 butter:40
Паэлья с морепродуктами|Паелья з морепродуктами|Seafood paella|rice:1000 shrimp:300 mussels:300 squid:200 pepper:200 tomato:250 oil:40
Чили с мясом и фасолью|Чилі з м’ясом та квасолею|Chili con carne|mince:600 beans:700 tomato:500 onion:200 pepper:250 oil:25
Фриттата с овощами|Фритата з овочами|Vegetable frittata|egg:700 zucchini:300 tomato:250 cheese:150 oil:25
Киш с курицей и грибами|Кіш із куркою та грибами|Chicken mushroom quiche|flour:350 butter:150 egg:300 chicken:350 mushroom:350 cream:300 cheese:150
''',250)
# Explicit end-of-cooking yield assumptions for baked/fried flour mixtures.
# No pretending the same weight remains after evaporation; values remain estimates.
for d in dishes:
    if d['category'] in ('Выпечка','Паста и пицца') and any(c['ingredient']=='flour' for c in d['ingredients']):
        d['finished_weight_g']=round(d['finished_weight_g']*.90,1)
    elif d['category']=='Десерты' and any(c['ingredient']=='flour' for c in d['ingredients']):
        d['finished_weight_g']=round(d['finished_weight_g']*.92,1)
assert len({d['names']['ru'] for d in dishes})==len(dishes)
# Do not falsely label approximate reference data as measured USDA records.
payload=dict(version=1,nutrition_basis='representative_recipe_estimate',method='Sum ingredient nutrition, divide by representative finished edible weight. Cooked ingredients use cooked weights. Oil is the estimated amount retained in the dish.',reference_note='SlimTrack internal reference estimates; not verified laboratory or manufacturer measurements. Actual recipe and cooked yield may differ.',methodology_reference='https://www.ars.usda.gov/ARSUserFiles/80400525/Data/retn/USDA_CookingYields_MeatPoultry.pdf',ingredients=ingredients,dishes=dishes)
(Path(__file__).resolve().parents[1] / 'overrides/assets/prepared_dishes.json').write_text(json.dumps(payload,ensure_ascii=False,indent=2))
from collections import Counter
print('Dishes:',len(dishes),'Ingredients:',len(ingredients))
print('By cuisine:',dict(Counter(d['cuisine'] for d in dishes)))
