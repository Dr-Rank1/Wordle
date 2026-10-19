import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def create_app_icon(output_path="play_store_assets/icon_512x512.png"):
    size = 512
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # 1. Background squircle / rounded rectangle
    bg_radius = 110
    # Outer dark background
    draw.rounded_rectangle([16, 16, 496, 496], radius=bg_radius, fill=(15, 20, 28, 255))
    
    # Subtle inner border / bezel
    draw.rounded_rectangle([16, 16, 496, 496], radius=bg_radius, outline=(40, 52, 70, 255), width=3)

    # 2. Subtle radial ambient glow in center
    glow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    gdraw = ImageDraw.Draw(glow)
    gdraw.ellipse([80, 80, 432, 432], fill=(46, 125, 50, 45))
    glow = glow.filter(ImageFilter.GaussianBlur(radius=50))
    img.paste(Image.alpha_composite(Image.new("RGBA", (size, size), (0, 0, 0, 0)), glow), (0, 0), glow)
    draw = ImageDraw.Draw(img)

    # 3. Main Center Tile: Extruded 3D Acrylic Tile in Vibrant Green ("N")
    # Tile coordinates: [116, 116, 396, 396] -> 280x280
    # Shadow layer
    shadow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    sdraw = ImageDraw.Draw(shadow)
    sdraw.rounded_rectangle([112, 136, 400, 424], radius=44, fill=(0, 0, 0, 160))
    shadow = shadow.filter(ImageFilter.GaussianBlur(radius=16))
    img.paste(Image.alpha_composite(Image.new("RGBA", (size, size), (0, 0, 0, 0)), shadow), (0, 0), shadow)
    draw = ImageDraw.Draw(img)

    # Extruded bottom lip (3D depth)
    draw.rounded_rectangle([116, 134, 396, 414], radius=42, fill=(27, 94, 32, 255))
    # Tile face (Emerald Green)
    draw.rounded_rectangle([116, 116, 396, 396], radius=42, fill=(46, 125, 50, 255))
    # Top specular rim highlight
    draw.rounded_rectangle([118, 118, 394, 134], radius=20, fill=(102, 187, 106, 180))

    # Bold Letter 'N'
    font_bold = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 190)
    # Letter shadow
    draw.text((256 + 2, 256 - 10 + 4), "N", font=font_bold, fill=(20, 60, 25, 180), anchor="mm")
    # Letter white face
    draw.text((256, 256 - 10), "N", font=font_bold, fill=(255, 255, 255, 255), anchor="mm")

    # 4. Secondary floating mini-tile (Top Left): Amber / Orange ("K" for Kisia)
    # Position: [40, 48, 140, 148] (100x100)
    # Mini shadow
    mini_shadow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    msdraw = ImageDraw.Draw(mini_shadow)
    msdraw.rounded_rectangle([38, 54, 142, 158], radius=24, fill=(0, 0, 0, 140))
    mini_shadow = mini_shadow.filter(ImageFilter.GaussianBlur(radius=8))
    img.paste(Image.alpha_composite(Image.new("RGBA", (size, size), (0, 0, 0, 0)), mini_shadow), (0, 0), mini_shadow)
    draw = ImageDraw.Draw(img)

    # Extruded lip
    draw.rounded_rectangle([42, 54, 138, 150], radius=22, fill=(230, 81, 0, 255))
    # Face
    draw.rounded_rectangle([42, 46, 138, 142], radius=22, fill=(245, 124, 0, 255))
    # Specular rim
    draw.rounded_rectangle([44, 48, 136, 60], radius=10, fill=(255, 183, 77, 180))
    # Letter K
    font_mini = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 62)
    draw.text((90, 94), "K", font=font_mini, fill=(255, 255, 255, 255), anchor="mm")

    # 5. Third floating mini-tile (Bottom Right): Slate Dark ("S")
    # Position: [372, 364, 472, 464] (100x100)
    mini_shadow2 = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    msdraw2 = ImageDraw.Draw(mini_shadow2)
    msdraw2.rounded_rectangle([370, 370, 474, 474], radius=24, fill=(0, 0, 0, 140))
    mini_shadow2 = mini_shadow2.filter(ImageFilter.GaussianBlur(radius=8))
    img.paste(Image.alpha_composite(Image.new("RGBA", (size, size), (0, 0, 0, 0)), mini_shadow2), (0, 0), mini_shadow2)
    draw = ImageDraw.Draw(img)

    # Extruded lip
    draw.rounded_rectangle([374, 372, 470, 468], radius=22, fill=(30, 41, 59, 255))
    # Face
    draw.rounded_rectangle([374, 364, 470, 460], radius=22, fill=(51, 65, 85, 255))
    # Specular rim
    draw.rounded_rectangle([376, 366, 468, 378], radius=10, fill=(100, 116, 139, 180))
    # Letter S
    draw.text((422, 412), "S", font=font_mini, fill=(255, 255, 255, 255), anchor="mm")

    img.save(output_path, "PNG", optimize=True)
    print(f"Icon generated at {output_path} ({size}x{size})")

create_app_icon()
