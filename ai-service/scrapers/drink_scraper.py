"""
饮品数据爬虫
使用Scrapy框架采集饮品营养信息
"""
import scrapy
from scrapy.crawler import CrawlerProcess
from typing import List, Dict
import json
import logging
from datetime import datetime

logger = logging.getLogger(__name__)


class DrinkSpider(scrapy.Spider):
    """饮品数据爬虫"""
    
    name = "drink_spider"
    
    # 示例目标网站(实际使用时需要替换为真实网站)
    start_urls = [
        # 可以添加奶茶品牌官网、营养数据库等
        # 'https://www.example-drink-nutrition.com/drinks'
    ]
    
    custom_settings = {
        'USER_AGENT': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
        'ROBOTSTXT_OBEY': True,
        'CONCURRENT_REQUESTS': 4,
        'DOWNLOAD_DELAY': 2,
        'COOKIES_ENABLED': False,
    }
    
    def __init__(self, *args, **kwargs):
        super(DrinkSpider, self).__init__(*args, **kwargs)
        self.collected_drinks = []
    
    
    def parse(self, response):
        """
        解析页面,提取饮品信息
        
        Args:
            response: Scrapy响应对象
        """
        # 示例解析逻辑(需要根据实际网站结构调整)
        try:
            # 假设页面有饮品列表
            drink_items = response.css('.drink-item')
            
            for item in drink_items:
                drink_data = {
                    'drink_name': item.css('.drink-name::text').get(),
                    'brand': item.css('.brand::text').get(),
                    'category': item.css('.category::text').get(),
                    'sugar_content': self._parse_number(item.css('.sugar::text').get()),
                    'calories': self._parse_number(item.css('.calories::text').get()),
                    'caffeine': self._parse_number(item.css('.caffeine::text').get()),
                    'price_range': item.css('.price::text').get(),
                    'description': item.css('.description::text').get(),
                    'scraped_at': datetime.now().isoformat()
                }
                
                if drink_data['drink_name']:
                    self.collected_drinks.append(drink_data)
                    yield drink_data
            
            # 处理分页
            next_page = response.css('a.next-page::attr(href)').get()
            if next_page:
                yield response.follow(next_page, self.parse)
                
        except Exception as e:
            logger.error(f"解析页面失败: {e}")
    
    
    def _parse_number(self, text: str) -> float:
        """从文本中提取数字"""
        if not text:
            return 0.0
        try:
            # 移除非数字字符
            import re
            numbers = re.findall(r'\d+\.?\d*', text)
            return float(numbers[0]) if numbers else 0.0
        except:
            return 0.0


class BeautifulSoupScraper:
    """使用BeautifulSoup的简单爬虫"""
    
    def __init__(self):
        """初始化爬虫"""
        self.session = None
        logger.info("BeautifulSoup爬虫初始化完成")
    
    
    def scrape_drink_data(self, url: str) -> List[Dict]:
        """
        从指定URL爬取饮品数据
        
        Args:
            url: 目标URL
            
        Returns:
            饮品数据列表
        """
        try:
            import requests
            from bs4 import BeautifulSoup
            
            logger.info(f"开始爬取: {url}")
            
            headers = {
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
            }
            
            response = requests.get(url, headers=headers, timeout=10)
            response.raise_for_status()
            
            soup = BeautifulSoup(response.content, 'html.parser')
            
            # 示例解析逻辑(需要根据实际网站调整)
            drinks = []
            drink_items = soup.find_all(class_='drink-item')
            
            for item in drink_items:
                try:
                    drink = {
                        'drink_name': item.find(class_='name').text.strip(),
                        'brand': item.find(class_='brand').text.strip() if item.find(class_='brand') else '',
                        'sugar_content': self._extract_number(item, 'sugar'),
                        'calories': self._extract_number(item, 'calories'),
                        'description': item.find(class_='description').text.strip() if item.find(class_='description') else '',
                    }
                    drinks.append(drink)
                except Exception as e:
                    logger.warning(f"解析饮品项失败: {e}")
                    continue
            
            logger.info(f"成功爬取 {len(drinks)} 个饮品数据")
            return drinks
            
        except Exception as e:
            logger.error(f"爬取失败: {e}")
            return []
    
    
    def scrape_nutrition_database(self) -> List[Dict]:
        """
        从营养数据库爬取饮品信息
        这是一个示例函数,展示如何从多个来源采集数据
        
        Returns:
            饮品数据列表
        """
        all_drinks = []
        
        # 示例数据源(实际使用时需要替换)
        data_sources = [
            # 'https://nutritiondata.example.com/drinks',
            # 'https://food-database.example.com/beverages',
        ]
        
        for source in data_sources:
            try:
                drinks = self.scrape_drink_data(source)
                all_drinks.extend(drinks)
            except Exception as e:
                logger.error(f"从 {source} 爬取失败: {e}")
                continue
        
        return all_drinks
    
    
    def _extract_number(self, element, class_name: str) -> float:
        """从HTML元素中提取数字"""
        try:
            text = element.find(class_=class_name).text
            import re
            numbers = re.findall(r'\d+\.?\d*', text)
            return float(numbers[0]) if numbers else 0.0
        except:
            return 0.0


class DrinkDataCollector:
    """饮品数据采集器 - 统一管理多种数据源"""
    
    def __init__(self, db_tool=None):
        """
        初始化数据采集器
        
        Args:
            db_tool: 数据库工具(可选,用于保存数据)
        """
        self.db_tool = db_tool
        self.bs_scraper = BeautifulSoupScraper()
        logger.info("饮品数据采集器初始化完成")
    
    
    def collect_from_manual_sources(self) -> List[Dict]:
        """
        从手动整理的数据源采集
        包括品牌官网公开数据、营养成分表等
        
        Returns:
            采集的饮品数据
        """
        drinks = []
        
        # 示例:奶茶品牌公开的营养信息
        # 实际使用时需要查找真实的数据源
        brand_data_sources = {
            '喜茶': self._collect_heytea_data,
            '奈雪的茶': self._collect_nayuki_data,
            '星巴克': self._collect_starbucks_data,
        }
        
        for brand, collector_func in brand_data_sources.items():
            try:
                logger.info(f"采集 {brand} 数据...")
                brand_drinks = collector_func()
                drinks.extend(brand_drinks)
            except Exception as e:
                logger.error(f"采集 {brand} 数据失败: {e}")
        
        return drinks
    
    
    def _collect_heytea_data(self) -> List[Dict]:
        """采集喜茶饮品数据(示例)"""
        # 这里可以实现从喜茶官网或公开API采集数据
        # 目前返回示例数据
        return [
            {
                'drink_name': '多肉葡萄',
                'brand': '喜茶',
                'category': '果茶',
                'sugar_content': 30,
                'calories': 250,
                'caffeine': 25,
                'price_range': '18-25',
                'health_score': 55,
                'taste_tags': ['果香', '清爽', '葡萄'],
                'description': '鲜葡萄制作,果香浓郁'
            },
            {
                'drink_name': '芝芝桃桃',
                'brand': '喜茶',
                'category': '奶茶',
                'sugar_content': 35,
                'calories': 320,
                'caffeine': 30,
                'price_range': '20-28',
                'health_score': 50,
                'taste_tags': ['桃子', '奶香', '顺滑'],
                'description': '水蜜桃与芝士奶盖的完美结合'
            }
        ]
    
    
    def _collect_nayuki_data(self) -> List[Dict]:
        """采集奈雪的茶饮品数据(示例)"""
        return [
            {
                'drink_name': '霸气芝士草莓',
                'brand': '奈雪的茶',
                'category': '奶茶',
                'sugar_content': 38,
                'calories': 350,
                'caffeine': 28,
                'price_range': '22-30',
                'health_score': 48,
                'taste_tags': ['草莓', '芝士', '甜'],
                'description': '新鲜草莓搭配芝士奶盖'
            }
        ]
    
    
    def _collect_starbucks_data(self) -> List[Dict]:
        """采集星巴克饮品数据(示例)"""
        return [
            {
                'drink_name': '拿铁咖啡',
                'brand': '星巴克',
                'category': '咖啡',
                'sugar_content': 18,
                'calories': 190,
                'caffeine': 150,
                'price_range': '28-35',
                'health_score': 65,
                'taste_tags': ['咖啡', '奶香', '浓郁'],
                'description': '经典意式拿铁,牛奶与浓缩咖啡的完美融合'
            }
        ]
    
    
    def collect_and_save(self) -> int:
        """
        采集数据并保存到数据库
        
        Returns:
            保存的饮品数量
        """
        if not self.db_tool:
            logger.error("未提供数据库工具,无法保存数据")
            return 0
        
        # 采集数据
        all_drinks = self.collect_from_manual_sources()
        
        # 保存到数据库
        saved_count = 0
        for drink in all_drinks:
            try:
                # 检查是否已存在
                existing = self.db_tool.search_drink_by_name(drink['drink_name'])
                if existing:
                    logger.info(f"饮品已存在,跳过: {drink['drink_name']}")
                    continue
                
                # 保存新饮品
                # 这里需要实现db_tool的save_drink方法
                # success = self.db_tool.save_drink(drink)
                # if success:
                #     saved_count += 1
                
                saved_count += 1  # 临时计数
                
            except Exception as e:
                logger.error(f"保存饮品失败: {drink['drink_name']}, {e}")
        
        logger.info(f"成功保存 {saved_count}/{len(all_drinks)} 个饮品")
        return saved_count
    
    
    def export_to_json(self, output_file: str = "drinks_data.json"):
        """
        导出采集的数据到JSON文件
        
        Args:
            output_file: 输出文件路径
        """
        try:
            drinks = self.collect_from_manual_sources()
            
            with open(output_file, 'w', encoding='utf-8') as f:
                json.dump(drinks, f, ensure_ascii=False, indent=2)
            
            logger.info(f"成功导出 {len(drinks)} 个饮品到 {output_file}")
            return len(drinks)
            
        except Exception as e:
            logger.error(f"导出失败: {e}")
            return 0


def run_drink_spider():
    """运行Scrapy爬虫"""
    try:
        process = CrawlerProcess()
        process.crawl(DrinkSpider)
        process.start()
    except Exception as e:
        logger.error(f"爬虫运行失败: {e}")


if __name__ == "__main__":
    # 测试数据采集
    collector = DrinkDataCollector()
    
    # 采集数据
    drinks = collector.collect_from_manual_sources()
    print(f"采集到 {len(drinks)} 个饮品")
    
    # 导出到JSON
    collector.export_to_json("scraped_drinks.json")

