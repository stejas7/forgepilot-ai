export default function BrandMark({small=false}:{small?:boolean}){
  return <span className={`pilot-mark${small?' small':''}`} aria-hidden="true"><i/><i/><i/></span>
}
